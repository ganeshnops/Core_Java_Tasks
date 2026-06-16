package com.hotel.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.hotel.enums.BookingStatus;
import com.hotel.enums.PaymentStatus;
import com.hotel.enums.RoomStatus;
import com.hotel.exception.BookingNotCancellableException;
import com.hotel.exception.HotelException;
import com.hotel.exception.InvalidCustomerException;
import com.hotel.exception.InvalidDateRangeException;
import com.hotel.exception.RoomNotAvailableException;
import com.hotel.model.Booking;
import com.hotel.model.Customer;
import com.hotel.model.Payment;
import com.hotel.model.Room;

/**
 * Booking orchestrator.
 * KEY: date-range overlap detection per room.
 * Uses synchronized block per-room to make the overlap check + insert atomic.
 */
public class BookingService {

    private final AtomicLong seq = new AtomicLong(70000);
    private final Map<String, Booking> bookings = new ConcurrentHashMap<>();
    /** Per-room booking list - cached for fast overlap check. */
    private final Map<String, List<Booking>> roomCalendar = new ConcurrentHashMap<>();
    /** Per-room lock for atomic check-and-insert. */
    private final Map<String, Object> roomLocks = new ConcurrentHashMap<>();

    private final RoomService roomService;
    private final CustomerService customerService;
    private final PricingService pricingService;
    private final PaymentService paymentService;
    private final NotificationService notify;

    // Revenue tracking
    private final Map<String, Double> dailyRevenue = new ConcurrentHashMap<>();
    private final Map<String, Double> hotelRevenue = new ConcurrentHashMap<>();
    private final Object revLock = new Object();

    public BookingService(RoomService rs, CustomerService cs, PricingService ps,
                          PaymentService payment, NotificationService notify) {
        this.roomService = rs;
        this.customerService = cs;
        this.pricingService = ps;
        this.paymentService = payment;
        this.notify = notify;
    }

    public Booking book(String customerId, String roomId,
                        LocalDate checkInDate, LocalDate checkOutDate) {
        // Validations
        Customer c = customerService.get(customerId);
        if (!c.isVerified()) {
            throw new InvalidCustomerException("Customer " + customerId + " is not verified.");
        }
        Room room = roomService.get(roomId);
        if (!room.isBookable()) {
            throw new RoomNotAvailableException("Room " + roomId + " is " + room.getStatus());
        }
        if (checkInDate.isBefore(LocalDate.now())) {
            throw new InvalidDateRangeException("Check-in date cannot be in the past.");
        }
        if (!checkInDate.isBefore(checkOutDate)) {
            throw new InvalidDateRangeException("Check-out must be after check-in.");
        }

        // Date overlap check + insert (atomic per-room)
        Object lock = roomLocks.computeIfAbsent(roomId, k -> new Object());
        synchronized (lock) {
            List<Booking> existing = roomCalendar.computeIfAbsent(roomId, k -> new ArrayList<>());
            for (Booking b : existing) {
                if (!b.holdsRoom()) continue;       // cancelled / checked-out don't count
                if (b.overlapsWith(checkInDate, checkOutDate)) {
                    throw new RoomNotAvailableException("Room " + roomId
                            + " already booked " + b.getCheckInDate() + " -> " + b.getCheckOutDate());
                }
            }

            // Calculate amount
            double subtotal = pricingService.calculateSubtotal(room, checkInDate, checkOutDate);
            double tax = pricingService.tax(subtotal);
            double total = subtotal + tax;

            String id = "BK-" + seq.incrementAndGet();
            Booking booking = new Booking(id, customerId, roomId, checkInDate, checkOutDate,
                    subtotal, tax, total);
            bookings.put(id, booking);
            existing.add(booking);

            // Process payment
            Payment payment = paymentService.charge(id, total);
            booking.setPaymentId(payment.getId());
            if (payment.getStatus() != PaymentStatus.SUCCESS) {
                booking.transitionTo(BookingStatus.CANCELLED);
                notify.notify(customerId, "Payment failed for booking " + id);
                throw new HotelException("Payment failed for booking " + id);
            }
            booking.transitionTo(BookingStatus.CONFIRMED);

            // Revenue tracking
            synchronized (revLock) {
                String day = booking.getBookedAt().toLocalDate().toString();
                dailyRevenue.merge(day, total, Double::sum);
                hotelRevenue.merge(room.getHotelId(), total, Double::sum);
            }

            notify.notify(customerId, "Booking " + id + " CONFIRMED. Total Rs." + total);
            return booking;
        }
    }

    public Booking checkIn(String bookingId) {
        Booking b = bookings.get(bookingId);
        if (b == null) throw new HotelException("Booking not found: " + bookingId);
        if (b.getStatus() != BookingStatus.CONFIRMED) {
            throw new HotelException("Booking " + bookingId + " not CONFIRMED.");
        }
        if (LocalDate.now().isBefore(b.getCheckInDate())) {
            throw new HotelException("Cannot check-in before " + b.getCheckInDate());
        }
        b.transitionTo(BookingStatus.CHECKED_IN);
        b.setActualCheckInTime(LocalDateTime.now());
        b.setKeyCardNumber("KEY-" + System.nanoTime());
        Room r = roomService.get(b.getRoomId());
        r.setStatus(RoomStatus.OCCUPIED);
        notify.notify(b.getCustomerId(), "Checked in. Key card: " + b.getKeyCardNumber());
        return b;
    }

    public Booking checkOut(String bookingId, int hoursLate) {
        Booking b = bookings.get(bookingId);
        if (b == null) throw new HotelException("Booking not found: " + bookingId);
        if (b.getStatus() != BookingStatus.CHECKED_IN) {
            throw new HotelException("Booking " + bookingId + " not CHECKED_IN.");
        }
        Room r = roomService.get(b.getRoomId());
        double late = pricingService.lateCheckOutFee(r, hoursLate);
        b.setExtraCharges(late);
        b.setActualCheckOutTime(LocalDateTime.now());
        b.transitionTo(BookingStatus.CHECKED_OUT);
        r.setStatus(RoomStatus.AVAILABLE);
        if (late > 0) {
            notify.notify(b.getCustomerId(), "Late checkout fee Rs." + late + " applied.");
        }
        notify.notify(b.getCustomerId(), "Checked out. Thank you!");
        return b;
    }

    public Booking cancel(String bookingId) {
        Booking b = bookings.get(bookingId);
        if (b == null) throw new HotelException("Booking not found: " + bookingId);
        if (b.getStatus() != BookingStatus.CONFIRMED && b.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BookingNotCancellableException("Cannot cancel in status: " + b.getStatus());
        }
        long daysToCheckIn = ChronoUnit.DAYS.between(LocalDate.now(), b.getCheckInDate());
        double refund = pricingService.refundAmount(b.getTotal(), daysToCheckIn);

        b.transitionTo(BookingStatus.CANCELLED);

        if (refund > 0 && b.getPaymentId() != null) {
            Payment p = paymentService.get(b.getPaymentId());
            if (p != null && p.getStatus() == PaymentStatus.SUCCESS) {
                paymentService.refund(p);
                b.transitionTo(BookingStatus.REFUNDED);
            }
        }
        notify.notify(b.getCustomerId(), "Booking " + bookingId + " cancelled. Refund Rs." + refund);
        return b;
    }

    public Booking get(String id) { return bookings.get(id); }
    public Collection<Booking> getAll() { return Collections.unmodifiableCollection(bookings.values()); }

    public Map<String, Double> getDailyRevenue() { return Collections.unmodifiableMap(dailyRevenue); }
    public Map<String, Double> getHotelRevenue() { return Collections.unmodifiableMap(hotelRevenue); }

    /** Find available rooms for the given date range. */
    public List<Room> availableRooms(String hotelId, LocalDate checkIn, LocalDate checkOut) {
        List<Room> out = new ArrayList<>();
        for (Room r : roomService.byHotel(hotelId)) {
            if (!r.isBookable()) continue;
            boolean busy = false;
            List<Booking> bs = roomCalendar.getOrDefault(r.getId(), Collections.emptyList());
            for (Booking b : bs) {
                if (b.holdsRoom() && b.overlapsWith(checkIn, checkOut)) { busy = true; break; }
            }
            if (!busy) out.add(r);
        }
        return out;
    }
}

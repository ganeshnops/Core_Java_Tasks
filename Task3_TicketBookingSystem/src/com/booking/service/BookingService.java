package com.booking.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.booking.enums.BookingStatus;
import com.booking.enums.PaymentStatus;
import com.booking.enums.SeatStatus;
import com.booking.exception.BookingException;
import com.booking.exception.BookingNotCancellableException;
import com.booking.exception.InvalidCustomerException;
import com.booking.exception.PaymentException;
import com.booking.exception.SeatNotAvailableException;
import com.booking.exception.ShowNotBookableException;
import com.booking.model.Booking;
import com.booking.model.Customer;
import com.booking.model.Payment;
import com.booking.model.Seat;
import com.booking.model.Show;

/**
 * Main booking orchestration.
 *  - Lock seats first (Concurrency rules).
 *  - Calculate amount using pricing service.
 *  - Apply coupon discount.
 *  - Process payment - if success then CONFIRM, if fail then release locks.
 *  - Cancel booking + refund + release seats.
 *  - Track revenue (per day, per theater, per movie).
 */
public class BookingService {

    public static final int MAX_SEATS_PER_BOOKING = 10;        // Booking rule 5
    public static final long PAYMENT_DEADLINE_MIN = 2;         // payment must finish in 2 min
    public static final long CANCEL_CUTOFF_HOURS = 1;          // can't cancel within 1 hour

    private final AtomicLong seq = new AtomicLong(100000);
    private final Map<String, Booking> bookings = new ConcurrentHashMap<>();

    private final Map<String, Double> dailyRevenue = new ConcurrentHashMap<>();
    private final Map<String, Double> theaterRevenue = new ConcurrentHashMap<>();
    private final Map<String, Double> movieRevenue = new ConcurrentHashMap<>();
    private final Map<String, Double> taxCollected = new ConcurrentHashMap<>();
    private double refundsTotal = 0;
    private final Object revenueLock = new Object();

    private final TheaterService theaterService;
    private final ShowService showService;
    private final SeatLockService seatLockService;
    private final CustomerService customerService;
    private final PricingService pricingService;
    private final CouponService couponService;
    private final PaymentService paymentService;
    private final NotificationService notificationService;

    public BookingService(TheaterService ts, ShowService ss, SeatLockService sls,
                          CustomerService cs, PricingService ps, CouponService cps,
                          PaymentService payment, NotificationService ns) {
        this.theaterService = ts;
        this.showService = ss;
        this.seatLockService = sls;
        this.customerService = cs;
        this.pricingService = ps;
        this.couponService = cps;
        this.paymentService = payment;
        this.notificationService = ns;
    }

    public Booking getBooking(String id) { return bookings.get(id); }
    public Collection<Booking> getAll()  { return Collections.unmodifiableCollection(bookings.values()); }

    /**
     * Main flow: select seats -> calculate -> lock -> payment -> confirm.
     * Returns the booking. Either CONFIRMED (success) or
     * throws (any error). Lock release / rollback handled inside.
     */
    public Booking book(String customerId, String showId, List<String> seatIds, String couponCode) {

        // ----- basic validations -----
        Customer customer = customerService.get(customerId);
        if (customer == null) throw new InvalidCustomerException("Customer not found: " + customerId);
        if (!customer.isEmailVerified() || !customer.isMobileVerified()) {
            throw new InvalidCustomerException("Customer email or mobile not verified.");
        }
        Show show = showService.get(showId);
        if (show == null) throw new BookingException("Show not found: " + showId);
        if (!show.canBeBookedNow()) {
            throw new ShowNotBookableException("Show " + showId + " is not bookable (status="
                    + show.getStatus() + ", time=" + show.getStartTime() + ").");
        }
        if (seatIds == null || seatIds.isEmpty()) throw new BookingException("At least one seat required.");
        if (seatIds.size() > MAX_SEATS_PER_BOOKING) {
            throw new BookingException("Max " + MAX_SEATS_PER_BOOKING + " seats per booking.");
        }
        // unique seat ids in this call
        Set<String> uniq = new HashSet<>(seatIds);
        if (uniq.size() != seatIds.size()) throw new BookingException("Duplicate seat ids in request.");

        // ----- check seats are not already booked / blocked / disabled -----
        for (String sid : seatIds) {
            Seat seat = theaterService.getSeat(sid);
            if (seat == null) throw new SeatNotAvailableException("Seat not found: " + sid);
            if (seat.getStatus() == SeatStatus.BLOCKED || seat.getStatus() == SeatStatus.DISABLED) {
                throw new SeatNotAvailableException("Seat " + sid + " is " + seat.getStatus());
            }
            if (show.getBookedSeats().containsKey(sid)) {
                throw new SeatNotAvailableException("Seat " + sid + " already booked for show " + showId);
            }
        }

        // ----- step 1: LOCK ALL seats -----
        if (!seatLockService.tryLockAll(showId, seatIds, customerId)) {
            throw new SeatNotAvailableException("One or more seats are being booked by another customer.");
        }

        try {
            // ----- step 2: compute price -----
            double subtotal = 0;
            for (String sid : seatIds) {
                Seat seat = theaterService.getSeat(sid);
                subtotal += pricingService.seatPrice(show, seat.getCategory());
            }
            double discount = (couponCode != null && !couponCode.isBlank())
                    ? couponService.validateAndCalculate(couponCode, subtotal) : 0;
            double afterDiscount = subtotal - discount;
            double tax = pricingService.tax(afterDiscount);
            double fee = pricingService.convenienceFee(seatIds.size());
            double total = round(afterDiscount + tax + fee);

            String bookingId = "BK-" + seq.incrementAndGet();
            LocalDateTime deadline = LocalDateTime.now().plusMinutes(PAYMENT_DEADLINE_MIN);
            Booking booking = new Booking(bookingId, customerId, showId, seatIds,
                    subtotal, tax, fee, discount, total, deadline);
            bookings.put(bookingId, booking);

            // ----- step 3: payment -----
            Payment payment;
            try {
                payment = paymentService.charge(bookingId, total, total);
            } catch (PaymentException ex) {
                booking.setStatus(BookingStatus.EXPIRED);
                seatLockService.releaseAll(showId, seatIds, customerId);
                throw ex;
            }
            booking.setPaymentId(payment.getId());

            if (payment.getStatus() != PaymentStatus.SUCCESS) {
                booking.setStatus(BookingStatus.EXPIRED);
                seatLockService.releaseAll(showId, seatIds, customerId);
                notificationService.notify(customerId, "Payment failed for booking " + bookingId);
                throw new PaymentException("Payment failed.");
            }

            // ----- step 4: commit seats -----
            for (String sid : seatIds) show.getBookedSeats().put(sid, bookingId);
            booking.setStatus(BookingStatus.CONFIRMED);

            // release the locks - they are no longer needed
            seatLockService.releaseAll(showId, seatIds, customerId);

            // ----- step 5: revenue tracking -----
            synchronized (revenueLock) {
                String day = booking.getCreatedAt().toLocalDate().toString();
                dailyRevenue.merge(day, total, Double::sum);
                taxCollected.merge(day, tax, Double::sum);
                String screenId = show.getScreenId();
                String theaterId = theaterService.getScreen(screenId).getTheaterId();
                theaterRevenue.merge(theaterId, total, Double::sum);
                movieRevenue.merge(show.getMovieId(), total, Double::sum);
            }

            // ----- step 6: notifications -----
            notificationService.notify(customerId, "Booking " + bookingId + " CONFIRMED. Total Rs." + total);
            return booking;

        } catch (RuntimeException ex) {
            seatLockService.releaseAll(showId, seatIds, customerId);
            throw ex;
        }
    }

    /** Cancel + refund. */
    public Booking cancel(String bookingId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) throw new BookingException("Booking not found: " + bookingId);
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BookingNotCancellableException("Booking " + bookingId + " not cancellable (status=" + booking.getStatus() + ")");
        }
        Show show = showService.get(booking.getShowId());
        long hoursBefore = ChronoUnit.HOURS.between(LocalDateTime.now(), show.getStartTime());
        if (hoursBefore < CANCEL_CUTOFF_HOURS) {
            throw new BookingNotCancellableException("Cannot cancel within " + CANCEL_CUTOFF_HOURS + " hour of show start.");
        }
        booking.getLock().lock();
        try {
            // release seats (Cancellation rule 2)
            for (String sid : booking.getSeatIds()) {
                show.getBookedSeats().remove(sid);
            }
            // refund (Cancellation rule 3, 4)
            Payment p = paymentService.getById(booking.getPaymentId());
            double refundAmt = pricingService.refundAmount(booking.getTotal(), hoursBefore);
            if (refundAmt > 0 && p != null) {
                String ref = paymentService.refund(p);
                synchronized (revenueLock) {
                    refundsTotal += refundAmt;
                }
                notificationService.notify(booking.getCustomerId(),
                        "Refund of Rs." + refundAmt + " issued (ref " + ref + ")");
                booking.setStatus(BookingStatus.REFUNDED);
            } else {
                booking.setStatus(BookingStatus.CANCELLED);
            }
            notificationService.notify(booking.getCustomerId(),
                    "Booking " + bookingId + " cancelled.");
            return booking;
        } finally {
            booking.getLock().unlock();
        }
    }

    /** Sweep expired pending-payment bookings (called by a scheduler). */
    public void sweepExpired() {
        for (Booking b : bookings.values()) {
            if (b.isPaymentExpired()) {
                b.setStatus(BookingStatus.EXPIRED);
                seatLockService.releaseAll(b.getShowId(), b.getSeatIds(), b.getCustomerId());
            }
        }
    }

    public Map<String, Double> getDailyRevenue()   { return Collections.unmodifiableMap(dailyRevenue); }
    public Map<String, Double> getTheaterRevenue() { return Collections.unmodifiableMap(theaterRevenue); }
    public Map<String, Double> getMovieRevenue()   { return Collections.unmodifiableMap(movieRevenue); }
    public Map<String, Double> getTaxCollected()   { return Collections.unmodifiableMap(taxCollected); }
    public double getRefundsTotal() {
        synchronized (revenueLock) { return refundsTotal; }
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    /** Helper for tests - list bookings of a customer (Customer rule 4 - booking history). */
    public List<Booking> getBookingsByCustomer(String customerId) {
        List<Booking> out = new ArrayList<>();
        for (Booking b : bookings.values()) {
            if (b.getCustomerId().equals(customerId)) out.add(b);
        }
        return out;
    }
}

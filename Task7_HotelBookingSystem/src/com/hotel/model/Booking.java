package com.hotel.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.hotel.enums.BookingStatus;
import com.hotel.exception.HotelException;

/**
 * One reservation. Pricing is captured at booking time so later rate changes
 * don't disturb existing reservations.
 *
 *  - constructor: (id, customerId, roomId, checkIn, checkOut, subtotal, tax, total)
 *  - overlapsWith / holdsRoom : used by BookingService for date-range conflict checks
 *  - transitionTo            : enforces the BookingStatus state machine
 *  - extraCharges            : populated at check-out for late-checkout fee
 */
public class Booking {

    private final String id;
    private final String customerId;
    private final String roomId;

    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private final long nights;

    /** Money snapshot at booking time. */
    private final double subtotal;
    private final double tax;
    private final double total;

    private final LocalDateTime bookedAt;

    private volatile BookingStatus status;
    private volatile String paymentId;
    private volatile String keyCardNumber;
    private volatile LocalDateTime actualCheckInTime;
    private volatile LocalDateTime actualCheckOutTime;
    private volatile double extraCharges;

    private final ReentrantLock lock = new ReentrantLock();

    public Booking(String id, String customerId, String roomId,
                   LocalDate checkInDate, LocalDate checkOutDate,
                   double subtotal, double tax, double total) {
        if (checkInDate == null || checkOutDate == null) {
            throw new HotelException("Check-in / check-out dates required.");
        }
        if (!checkInDate.isBefore(checkOutDate)) {
            throw new HotelException("Check-in must be before check-out.");
        }
        this.id = id;
        this.customerId = customerId;
        this.roomId = roomId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        this.subtotal = subtotal;
        this.tax = tax;
        this.total = total;
        this.bookedAt = LocalDateTime.now();
        this.status = BookingStatus.PENDING_PAYMENT;
    }

    // ---------- getters ----------
    public String getId()                       { return id; }
    public String getCustomerId()               { return customerId; }
    public String getRoomId()                   { return roomId; }
    public LocalDate getCheckInDate()           { return checkInDate; }
    public LocalDate getCheckOutDate()          { return checkOutDate; }
    public long getNights()                     { return nights; }
    public double getSubtotal()                 { return subtotal; }
    public double getTax()                      { return tax; }
    public double getTotal()                    { return total; }
    public LocalDateTime getBookedAt()          { return bookedAt; }
    public BookingStatus getStatus()            { return status; }
    public String getPaymentId()                { return paymentId; }
    public String getKeyCardNumber()            { return keyCardNumber; }
    public LocalDateTime getActualCheckInTime() { return actualCheckInTime; }
    public LocalDateTime getActualCheckOutTime(){ return actualCheckOutTime; }
    public double getExtraCharges()             { return extraCharges; }
    public ReentrantLock getLock()              { return lock; }

    // ---------- setters ----------
    public void setPaymentId(String p)              { this.paymentId = p; }
    public void setKeyCardNumber(String k)          { this.keyCardNumber = k; }
    public void setActualCheckInTime(LocalDateTime t)  { this.actualCheckInTime = t; }
    public void setActualCheckOutTime(LocalDateTime t) { this.actualCheckOutTime = t; }
    public void setExtraCharges(double c)           { this.extraCharges = c; }

    // ---------- behaviour ----------

    /** Move booking through its lifecycle. Rejects illegal transitions. */
    public void transitionTo(BookingStatus next) {
        lock.lock();
        try {
            if (!status.canTransitionTo(next)) {
                throw new HotelException("Booking " + id
                        + " cannot move " + status + " -> " + next);
            }
            this.status = next;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Half-open overlap with another date range.
     *   true  if [checkInDate, checkOutDate)  overlaps  [otherIn, otherOut)
     */
    public boolean overlapsWith(LocalDate otherIn, LocalDate otherOut) {
        return checkInDate.isBefore(otherOut) && otherIn.isBefore(checkOutDate);
    }

    /** True if this booking still occupies the room (CONFIRMED / CHECKED_IN / pending). */
    public boolean holdsRoom() {
        return status.blocksRoom();
    }

    @Override
    public String toString() {
        return String.format(
                "%s | cust=%s | room=%s | %s -> %s (%d nights) | Rs.%.2f | %s",
                id, customerId, roomId, checkInDate, checkOutDate, nights, total, status);
    }
}

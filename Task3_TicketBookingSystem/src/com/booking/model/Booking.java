package com.booking.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.booking.enums.BookingStatus;
import com.booking.exception.BookingException;

/**
 * One booking - covers one customer, one show, one-to-many seats.
 *  - id is unique (Rule 1 of booking).
 *  - must have at least 1 seat (Rule 2).
 *  - amount includes seat price + tax + fees - discount.
 *  - status transitions: PENDING_PAYMENT -> CONFIRMED / EXPIRED / CANCELLED -> REFUNDED.
 *  - lock used for thread-safe status mutation.
 */
public class Booking {

    private final String id;
    private final String customerId;
    private final String showId;
    private final List<String> seatIds;
    private final double subtotal;
    private final double tax;
    private final double convenienceFee;
    private final double discount;
    private final double total;
    private final LocalDateTime createdAt;
    private final LocalDateTime paymentDeadline;

    private volatile BookingStatus status;
    private volatile String paymentId;

    private final ReentrantLock lock = new ReentrantLock();

    public Booking(String id, String customerId, String showId, List<String> seatIds,
                   double subtotal, double tax, double convenienceFee, double discount,
                   double total, LocalDateTime paymentDeadline) {
        if (seatIds == null || seatIds.isEmpty()) {
            throw new BookingException("Booking must have at least one seat.");
        }
        this.id = id;
        this.customerId = customerId;
        this.showId = showId;
        this.seatIds = new ArrayList<>(seatIds);
        this.subtotal = subtotal;
        this.tax = tax;
        this.convenienceFee = convenienceFee;
        this.discount = discount;
        this.total = total;
        this.createdAt = LocalDateTime.now();
        this.paymentDeadline = paymentDeadline;
        this.status = BookingStatus.PENDING_PAYMENT;
    }

    public String getId()                       { return id; }
    public String getCustomerId()               { return customerId; }
    public String getShowId()                   { return showId; }
    public List<String> getSeatIds()            { return Collections.unmodifiableList(seatIds); }
    public double getSubtotal()                 { return subtotal; }
    public double getTax()                      { return tax; }
    public double getConvenienceFee()           { return convenienceFee; }
    public double getDiscount()                 { return discount; }
    public double getTotal()                    { return total; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public LocalDateTime getPaymentDeadline()   { return paymentDeadline; }
    public BookingStatus getStatus()            { return status; }
    public String getPaymentId()                { return paymentId; }

    public void setStatus(BookingStatus s)  { this.status = s; }
    public void setPaymentId(String id)      { this.paymentId = id; }

    public ReentrantLock getLock() { return lock; }

    public boolean isPaymentExpired() {
        return status == BookingStatus.PENDING_PAYMENT && LocalDateTime.now().isAfter(paymentDeadline);
    }

    @Override
    public String toString() {
        return String.format("%s | cust=%s | show=%s | seats=%d | total=Rs.%.2f | %s",
                id, customerId, showId, seatIds.size(), total, status);
    }
}

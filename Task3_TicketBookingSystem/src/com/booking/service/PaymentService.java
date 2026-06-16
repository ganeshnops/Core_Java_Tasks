package com.booking.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import com.booking.enums.PaymentStatus;
import com.booking.exception.PaymentException;
import com.booking.model.Payment;

public class PaymentService {

    private final AtomicLong seq = new AtomicLong(10000);
    private final Map<String, Payment> payments = new ConcurrentHashMap<>();

    public Payment getById(String id) { return payments.get(id); }

    /** Charge the customer. Simulated gateway with ~90% success. */
    public Payment charge(String bookingId, double amount, double expectedAmount) {
        if (Math.abs(amount - expectedAmount) > 0.01) {
            throw new PaymentException("Payment amount Rs." + amount
                    + " does not match expected Rs." + expectedAmount);
        }
        String id = "PAY-" + seq.incrementAndGet();
        String ref = "TXN-" + System.nanoTime();
        boolean ok = ThreadLocalRandom.current().nextInt(100) < 90;
        Payment p = new Payment(id, bookingId, amount, ok ? PaymentStatus.SUCCESS : PaymentStatus.FAILED, ref);
        payments.put(id, p);
        return p;
    }

    /** Issue a refund - returns the refund reference. */
    public String refund(Payment p) {
        if (p.getStatus() != PaymentStatus.SUCCESS) {
            throw new PaymentException("Cannot refund a non-success payment: " + p.getId());
        }
        String refundRef = "REF-" + System.nanoTime();
        p.setStatus(PaymentStatus.REFUNDED);
        p.setRefundRef(refundRef);
        return refundRef;
    }
}

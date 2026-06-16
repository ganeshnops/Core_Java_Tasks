package com.hotel.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import com.hotel.enums.PaymentStatus;
import com.hotel.exception.HotelException;
import com.hotel.model.Payment;

public class PaymentService {

    private final AtomicLong seq = new AtomicLong(50000);
    private final Map<String, Payment> payments = new ConcurrentHashMap<>();

    public Payment charge(String bookingId, double amount) {
        String id = "PAY-" + seq.incrementAndGet();
        String ref = "TXN-" + System.nanoTime();
        boolean ok = ThreadLocalRandom.current().nextInt(100) < 90;
        Payment p = new Payment(id, bookingId, amount,
                ok ? PaymentStatus.SUCCESS : PaymentStatus.FAILED, ref);
        payments.put(id, p);
        return p;
    }

    public String refund(Payment p) {
        if (p.getStatus() != PaymentStatus.SUCCESS) {
            throw new HotelException("Cannot refund non-success payment.");
        }
        String ref = "REF-" + System.nanoTime();
        p.setStatus(PaymentStatus.REFUNDED);
        p.setRefundRef(ref);
        return ref;
    }

    public Payment get(String id) { return payments.get(id); }
}

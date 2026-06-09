package com.food.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import com.food.enums.PaymentMode;
import com.food.enums.PaymentStatus;
import com.food.exception.PaymentFailedException;
import com.food.model.Payment;

/**
 * Payment processing.
 *  - Rule 22: COD orders within allowed limit.
 *  - Rule 23: failed payments cancel the order (handled in OrderService).
 *  - Rule 24: successful payments generate a transaction record.
 *  - Rule 45: every transaction has a unique reference number.
 *  - Rule 59: payment processing is idempotent - same idempotency key returns same Payment.
 */
public class PaymentService {

    /** COD limit (Rule 22). */
    public static final double COD_LIMIT = 2000.0;

    private final AtomicLong paymentSeq = new AtomicLong(10000);

    /** key = idempotency key, value = previously-completed Payment (Rule 59). */
    private final Map<String, Payment> idempotencyStore = new ConcurrentHashMap<>();

    /** All payments by paymentId. */
    private final Map<String, Payment> payments = new ConcurrentHashMap<>();

    public Payment getById(String id) { return payments.get(id); }

    /**
     * Process a payment idempotently.
     *  - If the same idempotency key has already been processed, return that Payment
     *    instead of creating a new one (Rule 59).
     */
    public Payment process(String orderId, double amount, PaymentMode mode, String idempotencyKey) {
        if (idempotencyKey != null) {
            Payment existing = idempotencyStore.get(idempotencyKey);
            if (existing != null) return existing;   // idempotent return
        }

        // COD limit
        if (mode == PaymentMode.COD && amount > COD_LIMIT) {
            throw new PaymentFailedException(
                    "COD limit is Rs." + COD_LIMIT + ". Order amount Rs." + amount + " not allowed.");
        }

        String paymentId = "PAY-" + paymentSeq.incrementAndGet();
        String txnRef = "TXN-" + System.nanoTime();
        PaymentStatus initial = (mode == PaymentMode.COD) ? PaymentStatus.PENDING : PaymentStatus.PENDING;
        Payment payment = new Payment(paymentId, orderId, amount, mode, initial, idempotencyKey, txnRef);

        // simulate prepaid gateway - 85% success
        if (mode == PaymentMode.PREPAID) {
            boolean ok = ThreadLocalRandom.current().nextInt(100) < 85;
            payment.setStatus(ok ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        } else {
            payment.setStatus(PaymentStatus.PENDING);   // COD - to be collected on delivery
        }

        payments.put(paymentId, payment);
        if (idempotencyKey != null) idempotencyStore.put(idempotencyKey, payment);
        return payment;
    }

    public void markRefunded(String paymentId) {
        Payment p = payments.get(paymentId);
        if (p != null) p.setStatus(PaymentStatus.REFUNDED);
    }
}

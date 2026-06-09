package com.food.model;

import java.time.LocalDateTime;

import com.food.enums.PaymentMode;
import com.food.enums.PaymentStatus;

/** Payment record - tied to one Order. */
public class Payment {

    private final String id;                 // unique payment id (Rule 45)
    private final String orderId;
    private final double amount;
    private final PaymentMode mode;
    private volatile PaymentStatus status;
    private final String idempotencyKey;     // Rule 59 - dedupe key
    private final String txnRef;             // gateway reference
    private final LocalDateTime createdAt;

    public Payment(String id, String orderId, double amount, PaymentMode mode,
                   PaymentStatus status, String idempotencyKey, String txnRef) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.mode = mode;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
        this.txnRef = txnRef;
        this.createdAt = LocalDateTime.now();
    }

    public String getId()              { return id; }
    public String getOrderId()         { return orderId; }
    public double getAmount()          { return amount; }
    public PaymentMode getMode()       { return mode; }
    public PaymentStatus getStatus()   { return status; }
    public String getIdempotencyKey()  { return idempotencyKey; }
    public String getTxnRef()          { return txnRef; }
    public LocalDateTime getCreatedAt(){ return createdAt; }

    public void setStatus(PaymentStatus s) { this.status = s; }

    @Override
    public String toString() {
        return String.format("PAY-%s | order=%s | Rs.%.2f | %s | %s | ref=%s",
                id, orderId, amount, mode, status, txnRef);
    }
}

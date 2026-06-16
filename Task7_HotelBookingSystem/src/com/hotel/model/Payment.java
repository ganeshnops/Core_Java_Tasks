package com.hotel.model;

import java.time.LocalDateTime;

import com.hotel.enums.PaymentStatus;

public class Payment {

    private final String id;
    private final String bookingId;
    private final double amount;
    private volatile PaymentStatus status;
    private final String txnRef;
    private final LocalDateTime createdAt;
    private volatile String refundRef;

    public Payment(String id, String bookingId, double amount, PaymentStatus status, String txnRef) {
        this.id = id;
        this.bookingId = bookingId;
        this.amount = amount;
        this.status = status;
        this.txnRef = txnRef;
        this.createdAt = LocalDateTime.now();
    }

    public String getId()             { return id; }
    public String getBookingId()      { return bookingId; }
    public double getAmount()         { return amount; }
    public PaymentStatus getStatus()  { return status; }
    public String getTxnRef()         { return txnRef; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
    public String getRefundRef()      { return refundRef; }

    public void setStatus(PaymentStatus s) { this.status = s; }
    public void setRefundRef(String r)     { this.refundRef = r; }

    @Override
    public String toString() {
        return String.format("%s | booking=%s | Rs.%.2f | %s | txn=%s",
                id, bookingId, amount, status, txnRef);
    }
}

package com.food.model;

import java.time.LocalDateTime;

/** Audit record for every wallet credit / debit (Rules 44, 45). */
public class WalletTransaction {

    private final String refNumber;
    private final String customerId;
    private final String type;        // CREDIT / DEBIT
    private final double amount;
    private final String note;
    private final LocalDateTime timestamp;

    public WalletTransaction(String refNumber, String customerId, String type,
                             double amount, String note) {
        this.refNumber = refNumber;
        this.customerId = customerId;
        this.type = type;
        this.amount = amount;
        this.note = note;
        this.timestamp = LocalDateTime.now();
    }

    public String getRefNumber() { return refNumber; }
    public String getCustomerId(){ return customerId; }
    public String getType()      { return type; }
    public double getAmount()    { return amount; }
    public String getNote()      { return note; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("%s | %s | %s Rs.%.2f | %s | %s",
                refNumber, customerId, type, amount, note, timestamp);
    }
}

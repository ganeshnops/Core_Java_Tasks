package com.atm.model;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

import com.atm.enums.TransactionStatus;
import com.atm.enums.TransactionType;

public class Transaction {

    private static final AtomicLong SEQ = new AtomicLong(10000);

    private final String id;
    private final TransactionType type;
    private final String fromAccount;
    private final String toAccount;          // null for non-transfer
    private final double amount;
    private volatile TransactionStatus status;
    private final LocalDateTime timestamp;
    private volatile String failureReason;

    public Transaction(TransactionType type, String fromAccount, String toAccount, double amount) {
        this.id = "TXN-" + SEQ.incrementAndGet();
        this.type = type;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.status = TransactionStatus.PENDING;
        this.timestamp = LocalDateTime.now();
    }

    public String getId()                  { return id; }
    public TransactionType getType()       { return type; }
    public String getFromAccount()         { return fromAccount; }
    public String getToAccount()           { return toAccount; }
    public double getAmount()              { return amount; }
    public TransactionStatus getStatus()   { return status; }
    public LocalDateTime getTimestamp()    { return timestamp; }
    public String getFailureReason()       { return failureReason; }

    public void markSuccess() { this.status = TransactionStatus.SUCCESS; }
    public void markFailed(String reason) {
        this.status = TransactionStatus.FAILED;
        this.failureReason = reason;
    }

    @Override
    public String toString() {
        return String.format("%s | %s | from=%s | to=%s | Rs.%.2f | %s | %s",
                id, type, fromAccount, toAccount == null ? "-" : toAccount, amount, status, timestamp);
    }
}

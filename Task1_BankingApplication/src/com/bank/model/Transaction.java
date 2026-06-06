package com.bank.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Immutable record of one transaction performed on an account.
 * Used in the account's transaction history (Collections + audit).
 */
public class Transaction {

    public enum Type { DEPOSIT, WITHDRAW, TRANSFER_IN, TRANSFER_OUT }

    // Auto-generated unique transaction id (thread-safe)
    private static final AtomicLong COUNTER = new AtomicLong(1000);

    private final long transactionId;
    private final Type type;
    private final double amount;
    private final double balanceAfter;
    private final String relatedAccount;   // for TRANSFER_* it's the other account
    private final LocalDateTime timestamp;

    public Transaction(Type type, double amount, double balanceAfter, String relatedAccount) {
        this.transactionId = COUNTER.incrementAndGet();
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.relatedAccount = relatedAccount;
        this.timestamp = LocalDateTime.now();
    }

    public long getTransactionId() { return transactionId; }
    public Type getType()          { return type; }
    public double getAmount()      { return amount; }
    public double getBalanceAfter(){ return balanceAfter; }
    public String getRelatedAccount() { return relatedAccount; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        String time = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String related = (relatedAccount == null) ? "" : " <-> " + relatedAccount;
        return String.format("TXN-%d | %s | Rs.%.2f | bal=Rs.%.2f%s | %s",
                transactionId, type, amount, balanceAfter, related, time);
    }
}

package com.atm.model;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

public class Session {

    private static final AtomicLong SEQ = new AtomicLong(1000);

    private final String id;
    private final String cardNumber;
    private final String accountNumber;
    private final LocalDateTime startedAt;
    private volatile long lastActivityEpochMs;

    public Session(String cardNumber, String accountNumber) {
        this.id = "SES-" + SEQ.incrementAndGet();
        this.cardNumber = cardNumber;
        this.accountNumber = accountNumber;
        this.startedAt = LocalDateTime.now();
        this.lastActivityEpochMs = System.currentTimeMillis();
    }

    public String getId()                  { return id; }
    public String getCardNumber()          { return cardNumber; }
    public String getAccountNumber()       { return accountNumber; }
    public LocalDateTime getStartedAt()    { return startedAt; }
    public long getLastActivityEpochMs()   { return lastActivityEpochMs; }

    public void touch() { this.lastActivityEpochMs = System.currentTimeMillis(); }

    public boolean isExpired(long maxIdleMillis) {
        return System.currentTimeMillis() - lastActivityEpochMs > maxIdleMillis;
    }

    @Override
    public String toString() {
        return String.format("%s | card=%s | account=%s | started=%s | lastActivity=%d ms ago",
                id, cardNumber, accountNumber, startedAt, System.currentTimeMillis() - lastActivityEpochMs);
    }
}

package com.food.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.food.exception.WalletException;

/**
 * Customer / user.
 *  - mobileVerified (Rule 6)
 *  - address must be non-empty for delivery (Rule 5)
 *  - blocked accounts cannot place orders (Rule 54)
 *  - walletBalance never negative (Rule 43)
 *  - wallet transactions are recorded and auditable (Rule 44)
 */
public class Customer {

    private final String id;
    private final String name;
    private final String mobile;
    private final boolean mobileVerified;
    private final String address;
    private volatile boolean blocked;

    private double walletBalance;
    private final List<WalletTransaction> walletTransactions = new ArrayList<>();
    private final ReentrantLock walletLock = new ReentrantLock();

    public Customer(String id, String name, String mobile, boolean mobileVerified,
                    String address, double initialWalletBalance) {
        this.id = id;
        this.name = name;
        this.mobile = mobile;
        this.mobileVerified = mobileVerified;
        this.address = address;
        this.walletBalance = Math.max(0, initialWalletBalance);
        if (initialWalletBalance > 0) {
            walletTransactions.add(new WalletTransaction(
                    customerWalletRef(), id, "CREDIT", initialWalletBalance, "Initial top-up"));
        }
    }

    public String getId()            { return id; }
    public String getName()          { return name; }
    public String getMobile()        { return mobile; }
    public boolean isMobileVerified(){ return mobileVerified; }
    public String getAddress()       { return address; }
    public boolean isBlocked()       { return blocked; }

    public void setBlocked(boolean b) { this.blocked = b; }

    public double getWalletBalance() {
        walletLock.lock();
        try { return walletBalance; }
        finally { walletLock.unlock(); }
    }

    public List<WalletTransaction> getWalletTransactions() {
        walletLock.lock();
        try { return Collections.unmodifiableList(new ArrayList<>(walletTransactions)); }
        finally { walletLock.unlock(); }
    }

    /** Add money to wallet (top-up or refund). */
    public void credit(double amount, String note) {
        if (amount <= 0) return;
        walletLock.lock();
        try {
            walletBalance += amount;
            walletTransactions.add(new WalletTransaction(
                    customerWalletRef(), id, "CREDIT", amount, note));
        } finally { walletLock.unlock(); }
    }

    /** Deduct money from wallet (payment). Throws if balance insufficient (Rule 43). */
    public void debit(double amount, String note) {
        if (amount <= 0) return;
        walletLock.lock();
        try {
            if (walletBalance < amount) {
                throw new WalletException("Insufficient wallet balance. Available: Rs." + walletBalance);
            }
            walletBalance -= amount;
            walletTransactions.add(new WalletTransaction(
                    customerWalletRef(), id, "DEBIT", amount, note));
        } finally { walletLock.unlock(); }
    }

    /** Tiny helper: generate a unique wallet ref. */
    private static String customerWalletRef() {
        return "WT-" + System.nanoTime();
    }

    @Override
    public String toString() {
        return String.format("%s [%s] mobile=%s verified=%s blocked=%s wallet=Rs.%.2f",
                id, name, mobile, mobileVerified, blocked, getWalletBalance());
    }
}

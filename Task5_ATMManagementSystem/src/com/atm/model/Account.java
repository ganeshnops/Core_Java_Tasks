package com.atm.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.atm.enums.AccountStatus;
import com.atm.enums.AccountType;
import com.atm.exception.ATMException;
import com.atm.exception.InsufficientBalanceException;

/**
 * Bank account.
 *  - Rule Account 1: unique number
 *  - Rule Account 2: balance never negative
 *  - Rule Account 3: maintain min balance
 *  - Rule Account 4: SAVINGS / CURRENT
 *  - Rule Account 5: closed accounts cannot transact
 *
 * Concurrency: ReentrantLock guards balance mutations. Transfers use external
 * lock ordering (smaller account number first) to prevent deadlock.
 */
public class Account {

    public static final double MIN_BALANCE_SAVINGS = 500;
    public static final double MIN_BALANCE_CURRENT = 1000;

    private final String accountNumber;
    private final String customerId;
    private final AccountType type;
    private volatile AccountStatus status;
    private double balance;
    private final ReentrantLock lock = new ReentrantLock();
    private final List<String> transactionRefs = new ArrayList<>();

    public Account(String accountNumber, String customerId, AccountType type, double openingBalance) {
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.type = type;
        this.balance = openingBalance;
        this.status = AccountStatus.ACTIVE;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getCustomerId()    { return customerId; }
    public AccountType getType()     { return type; }
    public AccountStatus getStatus() { return status; }

    public double getBalance() {
        lock.lock();
        try { return balance; }
        finally { lock.unlock(); }
    }

    public double getMinBalance() {
        return type == AccountType.SAVINGS ? MIN_BALANCE_SAVINGS : MIN_BALANCE_CURRENT;
    }

    public void setStatus(AccountStatus s) { this.status = s; }
    public ReentrantLock getLock() { return lock; }

    public List<String> getTransactionRefs() {
        lock.lock();
        try { return Collections.unmodifiableList(new ArrayList<>(transactionRefs)); }
        finally { lock.unlock(); }
    }

    /** Debit (assumes lock already held - used by TransferService for atomic transfer). */
    public void debitUnderLock(double amount, String txnId) {
        double remainingAfter = balance - amount;
        if (remainingAfter < getMinBalance()) {
            throw new InsufficientBalanceException(
                    "Insufficient balance in " + accountNumber + ". Min Rs." + getMinBalance()
                            + " must be maintained. Available: Rs." + balance);
        }
        balance = remainingAfter;
        transactionRefs.add(txnId);
    }

    public void creditUnderLock(double amount, String txnId) {
        balance += amount;
        transactionRefs.add(txnId);
    }

    public void debit(double amount, String txnId) {
        ensureActive();
        if (amount <= 0) throw new ATMException("Amount must be positive");
        lock.lock();
        try { debitUnderLock(amount, txnId); }
        finally { lock.unlock(); }
    }

    public void credit(double amount, String txnId) {
        ensureActive();
        if (amount <= 0) throw new ATMException("Amount must be positive");
        lock.lock();
        try { creditUnderLock(amount, txnId); }
        finally { lock.unlock(); }
    }

    public void ensureActive() {
        if (status == AccountStatus.BLOCKED) throw new ATMException("Account " + accountNumber + " is BLOCKED.");
        if (status == AccountStatus.CLOSED)  throw new ATMException("Account " + accountNumber + " is CLOSED.");
    }

    @Override
    public String toString() {
        return String.format("%s | cust=%s | %s | Rs.%.2f | %s",
                accountNumber, customerId, type, getBalance(), status);
    }
}

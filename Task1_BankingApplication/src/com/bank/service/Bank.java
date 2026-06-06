package com.bank.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import com.bank.exception.AccountNotFoundException;
import com.bank.exception.InvalidAmountException;
import com.bank.exception.MinimumDepositException;
import com.bank.model.Account;
import com.bank.model.CurrentAccount;
import com.bank.model.SavingsAccount;

/**
 * Bank service - owns all accounts and exposes thread-safe operations.
 *  - Collections : ConcurrentHashMap<String, Account>
 *  - Multithreading-safe : all mutating ops are synchronized at the right level
 *  - Deadlock Prevention : transfer locks accounts in a globally consistent order
 *                           (lower accountNumber first).
 */
public class Bank {

    /** Minimum opening deposit (Rule 1). */
    public static final double MIN_INITIAL_DEPOSIT = 1000.0;

    /** Account number generator - unique + auto-increment (Rule 6). */
    private final AtomicLong accountNumberSeq = new AtomicLong(1000);

    /** All accounts (key = account number). */
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    public enum AccountType { SAVINGS, CURRENT }

    /**
     * Create a new account.
     * Rule 1: initial deposit must be >= 1000.
     * Rule 6: account number is auto-generated and unique.
     */
    public Account createAccount(String holderName, AccountType type, double initialDeposit) {
        if (initialDeposit < MIN_INITIAL_DEPOSIT) {
            throw new MinimumDepositException(
                    "Initial deposit must be at least Rs." + MIN_INITIAL_DEPOSIT
                            + ". Got: Rs." + initialDeposit);
        }
        String accNo = "AC" + accountNumberSeq.incrementAndGet();
        Account acc = (type == AccountType.SAVINGS)
                ? new SavingsAccount(accNo, holderName, initialDeposit)
                : new CurrentAccount(accNo, holderName, initialDeposit);
        accounts.put(accNo, acc);
        return acc;
    }

    public Account getAccount(String accountNumber) {
        Account acc = accounts.get(accountNumber);
        if (acc == null) {
            throw new AccountNotFoundException("Account not found: " + accountNumber);
        }
        return acc;
    }

    public Collection<Account> getAllAccounts() {
        return Collections.unmodifiableCollection(accounts.values());
    }

    /** Convenience wrapper - thread-safe by virtue of Account.deposit. */
    public void deposit(String accountNumber, double amount) {
        getAccount(accountNumber).deposit(amount);
    }

    /** Convenience wrapper - thread-safe by virtue of Account.withdraw. */
    public void withdraw(String accountNumber, double amount) {
        getAccount(accountNumber).withdraw(amount);
    }

    /**
     * Transfer money from one account to another.
     *
     *  - Rule 5: amount must be positive
     *  - Rule 4: balance never goes negative (debit checks)
     *  - DEADLOCK PREVENTION: we lock the two accounts in a globally consistent
     *    order using their account numbers. Whichever thread calls transfer
     *    (A->B or B->A), both will lock the account with the smaller account
     *    number first, then the other. Two threads can never end up in a
     *    "hold-and-wait-for-each-other" cycle.
     */
    public void transfer(String fromAccNo, String toAccNo, double amount) {
        if (amount <= 0) {
            throw new InvalidAmountException("Transfer amount must be positive. Got: " + amount);
        }
        if (fromAccNo.equals(toAccNo)) {
            throw new InvalidAmountException("Cannot transfer to the same account: " + fromAccNo);
        }

        Account from = getAccount(fromAccNo);
        Account to   = getAccount(toAccNo);

        // Choose the locking order based on account number ordering.
        Account firstLock  = (fromAccNo.compareTo(toAccNo) < 0) ? from : to;
        Account secondLock = (firstLock == from) ? to : from;

        ReentrantLock l1 = firstLock.getLock();
        ReentrantLock l2 = secondLock.getLock();

        l1.lock();
        try {
            l2.lock();
            try {
                // perform debit + credit atomically while holding both locks
                from.debitForTransfer(amount, toAccNo);
                to.creditForTransfer(amount, fromAccNo);
            } finally {
                l2.unlock();
            }
        } finally {
            l1.unlock();
        }
    }
}

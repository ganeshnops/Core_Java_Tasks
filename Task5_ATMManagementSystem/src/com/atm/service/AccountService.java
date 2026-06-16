package com.atm.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.atm.enums.AccountStatus;
import com.atm.enums.AccountType;
import com.atm.exception.ATMException;
import com.atm.model.Account;

public class AccountService {

    private final AtomicLong seq = new AtomicLong(700000000L);
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    public Account open(String customerId, AccountType type, double openingBalance) {
        String accNo = String.valueOf(seq.incrementAndGet());
        Account acc = new Account(accNo, customerId, type, openingBalance);
        accounts.put(accNo, acc);
        return acc;
    }

    public Account get(String accountNumber) {
        Account a = accounts.get(accountNumber);
        if (a == null) throw new ATMException("Account not found: " + accountNumber);
        return a;
    }

    public Collection<Account> getAll() { return Collections.unmodifiableCollection(accounts.values()); }
    public void setStatus(String accountNumber, AccountStatus status) {
        get(accountNumber).setStatus(status);
    }
}

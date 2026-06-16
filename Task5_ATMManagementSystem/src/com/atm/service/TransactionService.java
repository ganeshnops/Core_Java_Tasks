package com.atm.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.atm.model.Transaction;

/**
 * Transaction logging + duplicate detection.
 *  - Rule Txn 1, 2: every transaction logged with unique ID (handled in Transaction).
 *  - Rule Txn 6: duplicate prevention via idempotency key (optional).
 *  - Rule Mini Statement 1, 2, 3: ordered history per account.
 */
public class TransactionService {

    private final Map<String, Transaction> all = new ConcurrentHashMap<>();
    private final Set<String> processedKeys = ConcurrentHashMap.newKeySet();

    public Transaction newTransaction(Transaction t) {
        all.put(t.getId(), t);
        return t;
    }

    public Transaction get(String id) { return all.get(id); }
    public Collection<Transaction> getAll() { return Collections.unmodifiableCollection(all.values()); }

    /** Mini statement: last N for account. */
    public List<Transaction> miniStatement(String accountNumber, int n) {
        List<Transaction> out = new ArrayList<>();
        for (Transaction t : all.values()) {
            if (accountNumber.equals(t.getFromAccount()) || accountNumber.equals(t.getToAccount())) {
                out.add(t);
            }
        }
        out.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));   // newest first
        if (out.size() > n) return out.subList(0, n);
        return out;
    }

    /** Idempotency check (Rule Txn 6). Returns true if NEW key (you can proceed). */
    public boolean tryRegisterKey(String idempotencyKey) {
        return processedKeys.add(idempotencyKey);
    }
}

package com.atm.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.atm.enums.Denomination;
import com.atm.exception.ATMException;
import com.atm.model.ATM;

/** Manages physical ATMs - admin can refill / view inventory. */
public class ATMService {

    private final Map<String, ATM> atms = new ConcurrentHashMap<>();

    public ATM addATM(ATM a) { atms.put(a.getId(), a); return a; }
    public ATM get(String id) {
        ATM a = atms.get(id);
        if (a == null) throw new ATMException("ATM not found: " + id);
        return a;
    }

    public Collection<ATM> getAll() { return Collections.unmodifiableCollection(atms.values()); }

    public void refill(String atmId, Denomination d, int notes, String adminId) {
        ATM a = get(atmId);
        a.refill(d, notes);
    }
}

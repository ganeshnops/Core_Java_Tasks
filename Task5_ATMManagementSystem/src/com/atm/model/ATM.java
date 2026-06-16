package com.atm.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import com.atm.enums.Denomination;
import com.atm.exception.InsufficientATMCashException;

/**
 * ATM machine with cash inventory.
 *  - Rule Cash 1: maintains note inventory per denomination.
 *  - Rule Cash 2: cash balance never goes negative.
 *  - Rule Cash 4: supports multiple denominations.
 *  - Rule Cash 5: low cash alerts.
 *  - Rule Cash 6: stops withdrawals when exhausted.
 *
 * Concurrency: dispensing must atomically reserve notes - we use a ReentrantLock.
 */
public class ATM {

    public static final int LOW_CASH_THRESHOLD = 20;   // notes per denomination

    private final String id;
    private final String location;
    private final Map<Denomination, AtomicInteger> noteInventory = new EnumMap<>(Denomination.class);
    private final ReentrantLock cashLock = new ReentrantLock();

    public ATM(String id, String location) {
        this.id = id;
        this.location = location;
        for (Denomination d : Denomination.values()) {
            noteInventory.put(d, new AtomicInteger(0));
        }
    }

    public String getId()       { return id; }
    public String getLocation() { return location; }

    public int notesOf(Denomination d) { return noteInventory.get(d).get(); }

    public int totalCash() {
        int total = 0;
        for (Denomination d : Denomination.values()) {
            total += noteInventory.get(d).get() * d.getValue();
        }
        return total;
    }

    /** Refill (admin) */
    public void refill(Denomination d, int notes) {
        cashLock.lock();
        try { noteInventory.get(d).addAndGet(notes); }
        finally { cashLock.unlock(); }
    }

    /**
     * Dispense the requested amount using a GREEDY denomination algorithm
     * (highest denomination first). Returns the breakdown.
     * Atomically reserves the notes. If impossible, throws InsufficientATMCashException
     * with no inventory changes.
     */
    public Map<Denomination, Integer> dispense(int amount) {
        cashLock.lock();
        try {
            Map<Denomination, Integer> plan = new EnumMap<>(Denomination.class);
            int remaining = amount;
            for (Denomination d : Denomination.values()) {
                int needed = remaining / d.getValue();
                int available = noteInventory.get(d).get();
                int take = Math.min(needed, available);
                if (take > 0) {
                    plan.put(d, take);
                    remaining -= take * d.getValue();
                }
            }
            if (remaining != 0) {
                throw new InsufficientATMCashException(
                        "ATM cannot dispense Rs." + amount + " with available denominations.");
            }
            // commit: subtract from inventory
            for (Map.Entry<Denomination, Integer> e : plan.entrySet()) {
                noteInventory.get(e.getKey()).addAndGet(-e.getValue());
            }
            return plan;
        } finally { cashLock.unlock(); }
    }

    public boolean isLowOnCash() {
        for (AtomicInteger ai : noteInventory.values()) {
            if (ai.get() <= LOW_CASH_THRESHOLD) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("ATM %s @ %s | total=Rs.%d | notes=%s",
                id, location, totalCash(), noteInventory);
    }
}

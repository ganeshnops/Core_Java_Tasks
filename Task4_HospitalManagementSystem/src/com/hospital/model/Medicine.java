package com.hospital.model;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Medicine in the pharmacy.
 *  - Rule Pharmacy 1: unique ID
 *  - Rule 2: expired medicines cannot be sold
 *  - Rule 3, 4: stock thread-safe (AtomicInteger)
 *  - Rule 5: low stock alerts
 *  - Rule 6: prescription-required medicines need valid prescription
 */
public class Medicine {

    public static final int LOW_STOCK_THRESHOLD = 10;

    private final String id;
    private final String name;
    private final double price;
    private final LocalDate expiry;
    private final boolean prescriptionRequired;
    private final AtomicInteger stock;

    public Medicine(String id, String name, double price, LocalDate expiry,
                    int initialStock, boolean prescriptionRequired) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.expiry = expiry;
        this.prescriptionRequired = prescriptionRequired;
        this.stock = new AtomicInteger(initialStock);
    }

    public String getId()           { return id; }
    public String getName()         { return name; }
    public double getPrice()        { return price; }
    public LocalDate getExpiry()    { return expiry; }
    public boolean isPrescriptionRequired() { return prescriptionRequired; }
    public int getStock()           { return stock.get(); }

    public boolean isExpired()      { return LocalDate.now().isAfter(expiry); }
    public boolean isLowStock()     { return stock.get() <= LOW_STOCK_THRESHOLD; }

    /** Sell qty - thread-safe via CAS. */
    public boolean sell(int qty) {
        if (qty <= 0) return false;
        while (true) {
            int current = stock.get();
            if (current < qty) return false;
            if (stock.compareAndSet(current, current - qty)) return true;
        }
    }

    public void restock(int qty) { if (qty > 0) stock.addAndGet(qty); }

    @Override
    public String toString() {
        return String.format("%s | %s | Rs.%.2f | exp=%s | stock=%d%s%s",
                id, name, price, expiry, stock.get(),
                prescriptionRequired ? " | RX" : "",
                isLowStock() ? " | LOW-STOCK!" : "");
    }
}

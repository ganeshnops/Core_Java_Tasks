package com.hospital.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.hospital.enums.BillStatus;
import com.hospital.exception.BillLockedException;

/**
 * Bill - line items + tax + discount.
 *  - Rule Billing 8: bills cannot be modified after payment (status PAID).
 */
public class Bill {

    public static final double TAX_RATE = 0.05;

    private final String id;
    private final String patientId;
    private final LocalDateTime createdAt;
    private final List<LineItem> items = new ArrayList<>();
    private double discount = 0;
    private volatile BillStatus status;
    private final ReentrantLock lock = new ReentrantLock();

    public Bill(String id, String patientId) {
        this.id = id;
        this.patientId = patientId;
        this.createdAt = LocalDateTime.now();
        this.status = BillStatus.PENDING;
    }

    public String getId()              { return id; }
    public String getPatientId()       { return patientId; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
    public BillStatus getStatus()      { return status; }
    public List<LineItem> getItems()   { return Collections.unmodifiableList(items); }
    public double getDiscount()        { return discount; }

    public double getSubtotal() {
        lock.lock();
        try {
            double sum = 0;
            for (LineItem it : items) sum += it.amount;
            return sum;
        } finally { lock.unlock(); }
    }

    public double getTaxAmount()      { return round(getSubtotal() * TAX_RATE); }
    public double getTotal()          { return round(getSubtotal() - discount + getTaxAmount()); }

    public void addItem(String description, double amount) {
        lock.lock();
        try {
            if (status == BillStatus.PAID) {
                throw new BillLockedException("Bill " + id + " is already paid; cannot modify.");
            }
            items.add(new LineItem(description, amount));
        } finally { lock.unlock(); }
    }

    public void applyDiscount(double amount, String authorizedBy) {
        lock.lock();
        try {
            if (status == BillStatus.PAID) {
                throw new BillLockedException("Bill " + id + " is already paid.");
            }
            if (authorizedBy == null || authorizedBy.isBlank()) {
                throw new BillLockedException("Discount requires authorization (Billing rule 6).");
            }
            this.discount = amount;
            items.add(new LineItem("Discount authorized by " + authorizedBy, -amount));
        } finally { lock.unlock(); }
    }

    public void markPaid() {
        lock.lock();
        try { this.status = BillStatus.PAID; }
        finally { lock.unlock(); }
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    public static final class LineItem {
        public final String description;
        public final double amount;
        public final LocalDateTime addedAt = LocalDateTime.now();
        public LineItem(String description, double amount) {
            this.description = description;
            this.amount = amount;
        }
        @Override
        public String toString() {
            return String.format("    %s | Rs.%.2f | %s", description, amount, addedAt);
        }
    }

    @Override
    public String toString() {
        return String.format("%s | patient=%s | subtotal=Rs.%.2f | discount=Rs.%.2f | tax=Rs.%.2f | total=Rs.%.2f | %s",
                id, patientId, getSubtotal(), discount, getTaxAmount(), getTotal(), status);
    }
}

package com.food.model;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.food.enums.DeliveryPartnerStatus;

/**
 * Delivery partner / Rider.
 *  - status: AVAILABLE / BUSY / INACTIVE (Rules 32, 35, 56)
 *  - distanceKm used for nearest-first assignment (Rule 33)
 *  - maxActiveOrders cap (Rule 34)
 */
public class DeliveryPartner {

    private final String id;
    private final String name;
    private volatile DeliveryPartnerStatus status;
    private final int maxActiveOrders;
    private final AtomicInteger activeOrders = new AtomicInteger(0);
    private final double distanceKm;             // distance from the restaurant

    private final AtomicInteger reviewCount = new AtomicInteger(0);
    private final AtomicLong totalRatingX100 = new AtomicLong(0);

    public DeliveryPartner(String id, String name, double distanceKm, int maxActiveOrders) {
        this.id = id;
        this.name = name;
        this.distanceKm = distanceKm;
        this.maxActiveOrders = maxActiveOrders;
        this.status = DeliveryPartnerStatus.AVAILABLE;
    }

    public String getId()                          { return id; }
    public String getName()                        { return name; }
    public DeliveryPartnerStatus getStatus()       { return status; }
    public int getActiveOrders()                   { return activeOrders.get(); }
    public int getMaxActiveOrders()                { return maxActiveOrders; }
    public double getDistanceKm()                  { return distanceKm; }

    public void setStatus(DeliveryPartnerStatus s) { this.status = s; }

    /** Try to take one more order. Atomic - safe under concurrency. */
    public boolean tryTakeOrder() {
        if (status != DeliveryPartnerStatus.AVAILABLE) return false;
        while (true) {
            int current = activeOrders.get();
            if (current >= maxActiveOrders) {
                status = DeliveryPartnerStatus.BUSY;
                return false;
            }
            if (activeOrders.compareAndSet(current, current + 1)) {
                if (current + 1 >= maxActiveOrders) status = DeliveryPartnerStatus.BUSY;
                return true;
            }
        }
    }

    /** Mark one order as done - released a slot. */
    public void releaseOrder() {
        int now = activeOrders.decrementAndGet();
        if (now < 0) activeOrders.set(0);
        if (status == DeliveryPartnerStatus.BUSY && activeOrders.get() < maxActiveOrders) {
            status = DeliveryPartnerStatus.AVAILABLE;
        }
    }

    public double getAverageRating() {
        int count = reviewCount.get();
        return count == 0 ? 0.0 : totalRatingX100.get() / 100.0 / count;
    }

    public void addRating(int rating) {
        if (rating < 1 || rating > 5) return;
        totalRatingX100.addAndGet(rating * 100L);
        reviewCount.incrementAndGet();
    }

    @Override
    public String toString() {
        return String.format("%s [%s] - %s - %dkm - %d/%d active - rating:%.2f",
                id, name, status, (int)distanceKm, activeOrders.get(), maxActiveOrders,
                getAverageRating());
    }
}

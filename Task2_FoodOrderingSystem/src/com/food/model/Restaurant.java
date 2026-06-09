package com.food.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.food.enums.RestaurantStatus;

/**
 * A restaurant on the platform.
 *  - status (OPEN / CLOSED / SUSPENDED) controls whether it accepts orders (Rules 1, 55).
 *  - capacitySemaphore is a counting semaphore - the number of *active* orders the
 *    restaurant can handle at once (Rule 2). When all permits are used, the next
 *    incoming order must wait (Rule 3).
 *  - Rating is recomputed after reviews (Rule 41).
 */
public class Restaurant {

    private final String id;
    private final String name;
    private volatile RestaurantStatus status;
    private final int maxActiveOrders;
    private final Semaphore capacitySemaphore;

    private final Map<String, MenuItem> menu = new ConcurrentHashMap<>();

    // rating
    private final AtomicInteger reviewCount = new AtomicInteger(0);
    private final AtomicLong totalRatingX100 = new AtomicLong(0);   // store as int*100 to avoid double races

    public Restaurant(String id, String name, int maxActiveOrders) {
        this.id = id;
        this.name = name;
        this.status = RestaurantStatus.OPEN;
        this.maxActiveOrders = maxActiveOrders;
        this.capacitySemaphore = new Semaphore(maxActiveOrders, true);  // fair = FIFO
    }

    public String getId()                 { return id; }
    public String getName()                { return name; }
    public RestaurantStatus getStatus()   { return status; }
    public int getMaxActiveOrders()       { return maxActiveOrders; }
    public int getActiveOrders()          { return maxActiveOrders - capacitySemaphore.availablePermits(); }
    public int getQueueLength()           { return capacitySemaphore.getQueueLength(); }
    public Map<String, MenuItem> getMenu(){ return menu; }

    public void setStatus(RestaurantStatus s) { this.status = s; }

    public void addMenuItem(MenuItem item) {
        menu.put(item.getId(), item);
    }

    public MenuItem getMenuItem(String menuItemId) {
        return menu.get(menuItemId);
    }

    /**
     * Acquire one capacity permit. If all permits are in use, the calling thread
     * waits on the semaphore queue until one is released (Rule 3).
     */
    public void reserveOrderSlot() throws InterruptedException {
        capacitySemaphore.acquire();
    }

    /** Try to acquire a slot without blocking - useful for tests. */
    public boolean tryReserveOrderSlot() {
        return capacitySemaphore.tryAcquire();
    }

    public void releaseOrderSlot() {
        capacitySemaphore.release();
    }

    public double getAverageRating() {
        int count = reviewCount.get();
        if (count == 0) return 0.0;
        return totalRatingX100.get() / 100.0 / count;
    }

    public void addRating(int rating1to5) {
        if (rating1to5 < 1 || rating1to5 > 5) return;
        totalRatingX100.addAndGet(rating1to5 * 100L);
        reviewCount.incrementAndGet();
    }

    @Override
    public String toString() {
        return String.format("%s [%s] - %s - active:%d/%d - rating:%.2f (%d reviews)",
                id, name, status, getActiveOrders(), maxActiveOrders,
                getAverageRating(), reviewCount.get());
    }
}

package com.food.model;

import java.util.concurrent.atomic.AtomicInteger;

import com.food.enums.MenuItemStatus;

/**
 * One dish on a restaurant's menu.
 *  - Encapsulation : fields private, accessors public.
 *  - Thread-safe inventory : AtomicInteger so multiple ordering threads can
 *    decrement / increment without losing updates (Rule 10, Rule 58).
 */
public class MenuItem {

    private final String id;
    private final String name;
    private final double price;
    private final String restaurantId;
    private final AtomicInteger inventory;            // current stock
    private volatile MenuItemStatus status;           // AVAILABLE/OUT_OF_STOCK/DISCONTINUED

    public MenuItem(String id, String name, double price, String restaurantId,
                    int initialStock, MenuItemStatus status) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.restaurantId = restaurantId;
        this.inventory = new AtomicInteger(initialStock);
        this.status = status;
    }

    public String getId()             { return id; }
    public String getName()           { return name; }
    public double getPrice()          { return price; }
    public String getRestaurantId()   { return restaurantId; }
    public int getInventory()         { return inventory.get(); }
    public MenuItemStatus getStatus() { return status; }

    public void setStatus(MenuItemStatus status) { this.status = status; }

    /**
     * Try to reserve {@code qty} units of stock.
     * Returns true if successful, false if not enough (Rule 10 - never negative).
     * Thread-safe via AtomicInteger CAS.
     */
    public boolean reserveStock(int qty) {
        if (qty <= 0) return false;
        while (true) {
            int current = inventory.get();
            if (current < qty) return false;
            if (inventory.compareAndSet(current, current - qty)) {
                if (current - qty == 0) status = MenuItemStatus.OUT_OF_STOCK;
                return true;
            }
        }
    }

    /** Restore stock (used on cancellation - Rule 30). */
    public void restoreStock(int qty) {
        if (qty <= 0) return;
        inventory.addAndGet(qty);
        if (status == MenuItemStatus.OUT_OF_STOCK) status = MenuItemStatus.AVAILABLE;
    }

    public boolean isOrderable() {
        return status == MenuItemStatus.AVAILABLE && inventory.get() > 0;
    }

    @Override
    public String toString() {
        return String.format("%s [%s] - Rs.%.2f (stock=%d, %s)",
                id, name, price, inventory.get(), status);
    }
}

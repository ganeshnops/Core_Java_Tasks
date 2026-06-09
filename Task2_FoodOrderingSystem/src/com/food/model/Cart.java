package com.food.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A customer's active cart.
 *  - one cart per customer at a time (Rule 11) - enforced by CartService.
 *  - items must belong to the SAME restaurant (Rule 12).
 *  - amount is recalculated on every add/remove (Rule 13).
 */
public class Cart {

    private final String customerId;
    private String restaurantId;                            // first item sets this
    private final Map<String, Integer> items = new LinkedHashMap<>(); // menuItemId -> qty
    private double amount;                                  // running subtotal

    public Cart(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerId()  { return customerId; }
    public String getRestaurantId(){ return restaurantId; }
    public Map<String, Integer> getItems() { return Collections.unmodifiableMap(items); }
    public double getAmount()      { return amount; }
    public boolean isEmpty()       { return items.isEmpty(); }

    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }

    /** Adds (or accumulates) qty of an item. The cart caller is responsible for sync. */
    public void addItem(String menuItemId, int qty, double pricePerUnit) {
        items.merge(menuItemId, qty, Integer::sum);
        amount += qty * pricePerUnit;
    }

    public void removeItem(String menuItemId, double pricePerUnit) {
        Integer qty = items.remove(menuItemId);
        if (qty != null) amount -= qty * pricePerUnit;
        if (items.isEmpty()) restaurantId = null;
    }

    public void clear() {
        items.clear();
        amount = 0;
        restaurantId = null;
    }

    @Override
    public String toString() {
        return String.format("Cart[%s] restaurant=%s items=%d amount=Rs.%.2f",
                customerId, restaurantId, items.size(), amount);
    }
}

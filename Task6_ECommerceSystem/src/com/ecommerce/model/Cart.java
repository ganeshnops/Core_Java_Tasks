package com.ecommerce.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One active cart per customer (Cart Rule 1).
 *  - Same restaurant constraint NOT here (e-commerce can have multiple sellers).
 *  - Duplicate product -> qty increases (Cart Rule 4).
 *  - Amount auto-updated (Cart Rule 5).
 */
public class Cart {

    private final String customerId;
    private final Map<String, Integer> items = new LinkedHashMap<>();   // productId -> qty
    private final Map<String, Double> unitPrices = new LinkedHashMap<>(); // productId -> price snapshot
    private double amount;

    public Cart(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerId()  { return customerId; }
    public Map<String, Integer> getItems() { return Collections.unmodifiableMap(items); }
    public Map<String, Double> getUnitPrices() { return Collections.unmodifiableMap(unitPrices); }
    public double getAmount()      { return amount; }
    public boolean isEmpty()       { return items.isEmpty(); }

    public void addOrIncrease(String productId, int qty, double unitPrice) {
        items.merge(productId, qty, Integer::sum);
        unitPrices.put(productId, unitPrice);
        amount += qty * unitPrice;
    }

    public void clear() {
        items.clear();
        unitPrices.clear();
        amount = 0;
    }

    @Override
    public String toString() {
        return String.format("Cart[%s] items=%d amount=Rs.%.2f", customerId, items.size(), amount);
    }
}

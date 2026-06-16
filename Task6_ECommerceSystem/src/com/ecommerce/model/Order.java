package com.ecommerce.model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.ecommerce.enums.OrderStatus;
import com.ecommerce.exception.OrderException;

public class Order {

    private final String id;
    private final String customerId;
    private final Map<String, Integer> items;     // productId -> qty
    private final Map<String, Double> unitPrices; // productId -> snapshot
    private final double subtotal;
    private final double tax;
    private final double shipping;
    private final double total;
    private final String shippingAddressId;
    private final LocalDateTime createdAt;
    private volatile OrderStatus status;
    private final ReentrantLock lock = new ReentrantLock();

    public Order(String id, String customerId, Map<String, Integer> items,
                 Map<String, Double> unitPrices, double subtotal, double tax,
                 double shipping, double total, String shippingAddressId) {
        if (items == null || items.isEmpty()) {
            throw new OrderException("Order must have at least one product.");
        }
        this.id = id;
        this.customerId = customerId;
        this.items = new LinkedHashMap<>(items);
        this.unitPrices = new LinkedHashMap<>(unitPrices);
        this.subtotal = subtotal;
        this.tax = tax;
        this.shipping = shipping;
        this.total = total;
        this.shippingAddressId = shippingAddressId;
        this.createdAt = LocalDateTime.now();
        this.status = OrderStatus.PENDING_PAYMENT;
    }

    public String getId()                      { return id; }
    public String getCustomerId()              { return customerId; }
    public Map<String, Integer> getItems()     { return Collections.unmodifiableMap(items); }
    public Map<String, Double> getUnitPrices() { return Collections.unmodifiableMap(unitPrices); }
    public double getSubtotal()                { return subtotal; }
    public double getTax()                     { return tax; }
    public double getShipping()                { return shipping; }
    public double getTotal()                   { return total; }
    public String getShippingAddressId()       { return shippingAddressId; }
    public LocalDateTime getCreatedAt()        { return createdAt; }
    public OrderStatus getStatus()             { return status; }

    public void transitionTo(OrderStatus next) {
        lock.lock();
        try {
            if (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED) {
                if (next != OrderStatus.DELIVERED) {
                    throw new OrderException("Cannot modify order after shipment.");
                }
            }
            if (!status.canTransitionTo(next)) {
                throw new OrderException("Invalid status transition: " + status + " -> " + next);
            }
            this.status = next;
        } finally { lock.unlock(); }
    }

    @Override
    public String toString() {
        return String.format("%s | cust=%s | items=%d | total=Rs.%.2f | %s | %s",
                id, customerId, items.size(), total, status, createdAt);
    }
}

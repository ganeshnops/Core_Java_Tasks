package com.ecommerce.service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import com.ecommerce.enums.OrderStatus;
import com.ecommerce.exception.CartException;
import com.ecommerce.exception.CustomerException;
import com.ecommerce.exception.OrderException;
import com.ecommerce.model.Cart;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;

public class OrderService {

    public static final double TAX_RATE = 0.05;
    public static final double SHIPPING_CHARGE = 50.0;
    public static final double FREE_SHIPPING_THRESHOLD = 1000.0;

    private final AtomicLong seq = new AtomicLong(100000);
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final Set<String> processedIdempotencyKeys = ConcurrentHashMap.newKeySet();

    private final CustomerService customerService;
    private final CartService cartService;
    private final InventoryService inventoryService;

    public OrderService(CustomerService cs, CartService cart, InventoryService inv) {
        this.customerService = cs;
        this.cartService = cart;
        this.inventoryService = inv;
    }

    /**
     * Checkout flow:
     *  1. Validate customer + cart
     *  2. RESERVE inventory (two-phase part 1)
     *  3. Calculate amount
     *  4. Create order PENDING_PAYMENT
     *  5. Simulate payment - if success: COMMIT inventory + CONFIRMED
     *                       if fail   : RELEASE inventory + status CANCELLED
     */
    public Order checkout(String customerId, String idempotencyKey) {
        Customer customer = customerService.get(customerId);
        if (!customer.canOrder()) {
            throw new CustomerException("Customer cannot order (status=" + customer.getStatus() + " / no default address)");
        }
        Cart cart = cartService.getCart(customerId);
        if (cart == null || cart.isEmpty()) {
            throw new CartException("Cart is empty.");
        }

        // Idempotency (Order rule 5)
        if (idempotencyKey != null && !processedIdempotencyKeys.add(idempotencyKey)) {
            // Find existing
            for (Order o : orders.values()) {
                if (idempotencyKey.equals(customerId + "|" + o.getId())) return o;
            }
        }

        // Reserve inventory
        Map<String, Integer> reserved = inventoryService.reserveAll(new HashMap<>(cart.getItems()));

        double subtotal = cart.getAmount();
        double tax = round(subtotal * TAX_RATE);
        double shipping = subtotal >= FREE_SHIPPING_THRESHOLD ? 0 : SHIPPING_CHARGE;
        double total = round(subtotal + tax + shipping);

        String orderId = "ORD-" + seq.incrementAndGet();
        Order order = new Order(orderId, customerId, cart.getItems(), cart.getUnitPrices(),
                subtotal, tax, shipping, total, customer.getDefaultAddress().getId());
        orders.put(orderId, order);

        // Simulate payment - 90% success
        boolean paymentOk = ThreadLocalRandom.current().nextInt(100) < 90;
        if (paymentOk) {
            inventoryService.commitAll(reserved);
            order.transitionTo(OrderStatus.CONFIRMED);
            cartService.removeCart(customerId);
        } else {
            inventoryService.releaseAll(reserved);
            order.transitionTo(OrderStatus.CANCELLED);
            throw new OrderException("Payment failed for order " + orderId);
        }
        return order;
    }

    public Order get(String id) {
        Order o = orders.get(id);
        if (o == null) throw new OrderException("Order not found: " + id);
        return o;
    }

    public Collection<Order> getAll() { return Collections.unmodifiableCollection(orders.values()); }

    public void advance(String orderId, OrderStatus next) {
        get(orderId).transitionTo(next);
    }

    private static double round(double v) { return Math.round(v * 100.0) / 100.0; }
}

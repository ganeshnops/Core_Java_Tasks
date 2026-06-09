package com.food.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.food.enums.OrderStatus;
import com.food.enums.PaymentMode;
import com.food.enums.PaymentStatus;
import com.food.enums.RestaurantStatus;
import com.food.exception.CartException;
import com.food.exception.DuplicateOrderException;
import com.food.exception.FoodOrderException;
import com.food.exception.InvalidCustomerException;
import com.food.exception.MenuItemUnavailableException;
import com.food.exception.MinimumOrderValueException;
import com.food.exception.PaymentFailedException;
import com.food.exception.RestaurantNotOpenException;
import com.food.model.Cart;
import com.food.model.Customer;
import com.food.model.DeliveryPartner;
import com.food.model.MenuItem;
import com.food.model.Order;
import com.food.model.Payment;
import com.food.model.Restaurant;

/**
 * Main order coordination service. Brings together cart, restaurant capacity,
 * inventory, coupon, payment, delivery and notifications.
 *
 * Covers (among others):
 *   Rule 1, 55  Restaurant must be OPEN / not SUSPENDED
 *   Rule 2, 3   Restaurant capacity + waiting queue
 *   Rule 4      Minimum order value
 *   Rule 5, 6, 54  Customer valid / verified / not blocked
 *   Rule 13, 14 Tax + delivery + discount calculation
 *   Rule 19, 60 Duplicate order prevention (idempotency key)
 *   Rule 20     Unique order id (AtomicLong)
 *   Rule 21     Payment before confirmation (PREPAID)
 *   Rule 22, 23 COD limit, failed payment cancels order
 *   Rule 24     Payment generates transaction
 *   Rule 25, 26 Valid status transitions only
 *   Rule 27     Cancel only before preparation
 *   Rule 28, 29 Refund eligible (full / partial)
 *   Rule 30     Inventory restored on cancellation
 *   Rule 31     Delivery partner assigned automatically when READY
 *   Rule 36-38  Notifications
 *   Rule 46     Audit log on failure
 *   Rule 47     Order history retained
 *   Rule 57     Concurrent ordering supported safely
 *   Rule 60     Duplicate transaction prevention
 */
public class OrderService {

    public static final double MIN_ORDER_VALUE   = 100.0;
    public static final double TAX_RATE          = 0.05;   // 5%
    public static final double DELIVERY_CHARGE   = 30.0;
    public static final double HIGH_VALUE_THRESHOLD = 5000.0; // Rule 52

    /** Mana commission - Rule 50. */
    public static final double RESTAURANT_COMMISSION_RATE = 0.15;
    /** Platform commission - Rule 51 (tracked separately). */
    public static final double PLATFORM_COMMISSION_RATE   = 0.05;

    private final AtomicLong orderSeq = new AtomicLong(10000);
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final Set<String> processedIdempotencyKeys = ConcurrentHashMap.newKeySet();

    /** Audit log of every failed order attempt (Rule 46). */
    private final List<String> auditLog = Collections.synchronizedList(new ArrayList<>());

    /** Daily revenue totals (Rule 48). */
    private final Map<String, Double> dailyRevenue = new ConcurrentHashMap<>();
    /** Counts per menu item across orders (Rule 49). */
    private final Map<String, Integer> itemSalesCount = new ConcurrentHashMap<>();
    private double platformCommissionTotal = 0;
    private final Object commissionLock = new Object();

    private final RestaurantService restaurantService;
    private final CustomerService customerService;
    private final CartService cartService;
    private final CouponService couponService;
    private final PaymentService paymentService;
    private final DeliveryService deliveryService;
    private final NotificationService notificationService;

    public OrderService(RestaurantService rs, CustomerService cs, CartService cts,
                        CouponService cps, PaymentService ps, DeliveryService ds,
                        NotificationService ns) {
        this.restaurantService = rs;
        this.customerService   = cs;
        this.cartService       = cts;
        this.couponService     = cps;
        this.paymentService    = ps;
        this.deliveryService   = ds;
        this.notificationService = ns;
    }

    public Order getOrder(String id)                 { return orders.get(id); }
    public Collection<Order> getAllOrders()          { return Collections.unmodifiableCollection(orders.values()); }
    public List<String> getAuditLog()                { return Collections.unmodifiableList(auditLog); }
    public Map<String, Double> getDailyRevenue()     { return Collections.unmodifiableMap(dailyRevenue); }
    public Map<String, Integer> getItemSalesCount()  { return Collections.unmodifiableMap(itemSalesCount); }
    public double getPlatformCommissionTotal() {
        synchronized (commissionLock) { return platformCommissionTotal; }
    }

    /**
     * Place an order from the customer's cart.
     *
     * @param idempotencyKey  optional dedupe key. If the same key is presented twice,
     *                        the second attempt returns the existing order (Rule 19, 60).
     */
    public Order placeOrder(String customerId, PaymentMode paymentMode,
                            String couponCode, String idempotencyKey) {

        // ----- 1) basic validations -----
        Customer customer = customerService.get(customerId);
        if (customer == null) {
            throw new InvalidCustomerException("Customer not found: " + customerId);
        }
        if (customer.isBlocked()) {
            audit("Blocked customer attempt: " + customerId);
            throw new InvalidCustomerException("Customer is blocked: " + customerId);
        }
        if (!customer.isMobileVerified()) {
            throw new InvalidCustomerException("Mobile not verified: " + customer.getMobile());
        }
        if (customer.getAddress() == null || customer.getAddress().isBlank()) {
            throw new InvalidCustomerException("Customer has no delivery address: " + customerId);
        }

        Cart cart = cartService.getCart(customerId);
        if (cart == null || cart.isEmpty()) {
            throw new CartException("Cart is empty for customer " + customerId);
        }
        if (cart.getAmount() < MIN_ORDER_VALUE) {
            throw new MinimumOrderValueException("Minimum order value Rs." + MIN_ORDER_VALUE
                    + ". Cart total Rs." + cart.getAmount());
        }

        Restaurant restaurant = restaurantService.get(cart.getRestaurantId());
        if (restaurant == null) {
            throw new CartException("Restaurant not found: " + cart.getRestaurantId());
        }
        if (restaurant.getStatus() == RestaurantStatus.CLOSED) {
            throw new RestaurantNotOpenException("Restaurant " + restaurant.getId() + " is CLOSED.");
        }
        if (restaurant.getStatus() == RestaurantStatus.SUSPENDED) {
            audit("Suspended restaurant " + restaurant.getId() + " - rejected order");
            throw new RestaurantNotOpenException("Restaurant " + restaurant.getId() + " is SUSPENDED.");
        }

        // ----- 2) idempotency check (Rule 19, 60) -----
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            if (!processedIdempotencyKeys.add(idempotencyKey)) {
                // Already used - find and return the existing order
                for (Order o : orders.values()) {
                    if (idempotencyKey.equals(o.getIdempotencyKey())) return o;
                }
                throw new DuplicateOrderException("Duplicate request for key: " + idempotencyKey);
            }
        }

        // ----- 3) reserve capacity (Rule 2, 3) -----
        try {
            restaurant.reserveOrderSlot();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new FoodOrderException("Interrupted while waiting for restaurant slot.");
        }

        boolean inventoryReserved = false;
        Map<String, Integer> takenSnapshot = new HashMap<>();   // for rollback if needed
        try {
            // ----- 4) reserve inventory for each item (Rule 10) -----
            for (Map.Entry<String, Integer> entry : cart.getItems().entrySet()) {
                MenuItem item = restaurant.getMenuItem(entry.getKey());
                if (item == null || !item.reserveStock(entry.getValue())) {
                    // rollback what we already took
                    for (Map.Entry<String, Integer> taken : takenSnapshot.entrySet()) {
                        MenuItem t = restaurant.getMenuItem(taken.getKey());
                        if (t != null) t.restoreStock(taken.getValue());
                    }
                    throw new MenuItemUnavailableException("Item unavailable / out of stock: " + entry.getKey());
                }
                takenSnapshot.put(entry.getKey(), entry.getValue());
            }
            inventoryReserved = true;

            // ----- 5) calculate amount (Rule 13, 14) -----
            double subtotal = cart.getAmount();
            double tax = round(subtotal * TAX_RATE);
            double delivery = DELIVERY_CHARGE;
            double discount = 0;
            if (couponCode != null && !couponCode.isBlank()) {
                discount = round(couponService.validateAndCalculate(couponCode, subtotal));
            }
            double total = round(subtotal + tax + delivery - discount);

            // Rule 52 - high value flag (audit)
            if (total > HIGH_VALUE_THRESHOLD) {
                audit("High value order Rs." + total + " for customer " + customerId);
            }

            // ----- 6) create order -----
            String orderId = "ORD-" + orderSeq.incrementAndGet();
            Order order = new Order(orderId, customerId, restaurant.getId(),
                    cart.getItems(), subtotal, tax, delivery, discount, total,
                    paymentMode, idempotencyKey);
            orders.put(orderId, order);

            // notify customer + restaurant (Rules 36, 37)
            notificationService.notifyRestaurant(restaurant.getId(), orderId,
                    "New order received - Rs." + total);
            notificationService.notifyCustomer(customerId, orderId,
                    "Order placed - Rs." + total + " - awaiting payment");

            // ----- 7) process payment (Rule 21, 22, 23, 24) -----
            Payment payment = paymentService.process(orderId, total, paymentMode, idempotencyKey);
            order.setPaymentId(payment.getId());

            if (paymentMode == PaymentMode.PREPAID) {
                if (payment.getStatus() != PaymentStatus.SUCCESS) {
                    // Rule 23 - cancel order + restore inventory
                    rollbackInventory(restaurant, takenSnapshot);
                    inventoryReserved = false;
                    order.transitionTo(OrderStatus.CANCELLED);
                    audit("Payment failed for order " + orderId + " - cancelled");
                    notificationService.notifyCustomer(customerId, orderId, "Payment failed - order cancelled");
                    throw new PaymentFailedException("Payment failed for order " + orderId);
                }
                order.transitionTo(OrderStatus.CONFIRMED);
            } else {
                // COD - confirmed pending collection on delivery
                order.transitionTo(OrderStatus.CONFIRMED);
            }
            notificationService.notifyCustomer(customerId, orderId, "Order CONFIRMED");

            // ----- 8) record revenue + sales (Rule 48, 49, 50, 51) -----
            String day = order.getCreatedAt().toLocalDate().toString();
            dailyRevenue.merge(day, total, Double::sum);
            for (Map.Entry<String, Integer> e : cart.getItems().entrySet()) {
                itemSalesCount.merge(e.getKey(), e.getValue(), Integer::sum);
            }
            synchronized (commissionLock) {
                platformCommissionTotal += round(total * PLATFORM_COMMISSION_RATE);
            }

            // ----- 9) clear cart -----
            cartService.removeCart(customerId);
            return order;

        } catch (RuntimeException ex) {
            if (inventoryReserved) rollbackInventory(restaurant, takenSnapshot);
            // capacity slot will be released in finally below if capacity was reserved
            // we don't release the order until status is OUT_FOR_DELIVERY/DELIVERED/CANCELLED
            // but if the order was never confirmed we should release the slot
            restaurant.releaseOrderSlot();
            throw ex;
        }
    }

    private void rollbackInventory(Restaurant restaurant, Map<String, Integer> taken) {
        for (Map.Entry<String, Integer> e : taken.entrySet()) {
            MenuItem t = restaurant.getMenuItem(e.getKey());
            if (t != null) t.restoreStock(e.getValue());
        }
    }

    /**
     * Move the order through PREPARING -> READY -> OUT_FOR_DELIVERY -> DELIVERED.
     * Each call advances one step (for demo). Rule 25, 26 enforced inside Order.
     */
    public void advanceStatus(String orderId, OrderStatus next) {
        Order order = orders.get(orderId);
        if (order == null) throw new FoodOrderException("Order not found: " + orderId);

        order.transitionTo(next);
        notificationService.notifyCustomer(order.getCustomerId(), orderId, "Order status -> " + next);

        // when READY, assign delivery partner (Rule 31)
        if (next == OrderStatus.READY) {
            DeliveryPartner dp = deliveryService.assignNearest();
            order.setDeliveryPartnerId(dp.getId());
            notificationService.notifyDeliveryPartner(dp.getId(), orderId,
                    "New pickup assignment - " + order.getRestaurantId() + " -> customer " + order.getCustomerId());
        }
        if (next == OrderStatus.DELIVERED || next == OrderStatus.CANCELLED) {
            // free the restaurant capacity slot
            Restaurant r = restaurantService.get(order.getRestaurantId());
            if (r != null) r.releaseOrderSlot();
            // free the delivery partner slot if any
            if (order.getDeliveryPartnerId() != null) {
                deliveryService.release(order.getDeliveryPartnerId());
            }
        }
    }

    /**
     * Cancel an order (Rule 27 - only before preparation).
     * Restores inventory (Rule 30), initiates refund if eligible (Rules 28, 29).
     */
    public void cancelOrder(String orderId) {
        Order order = orders.get(orderId);
        if (order == null) throw new FoodOrderException("Order not found: " + orderId);

        if (!order.isCancellable()) {
            throw new FoodOrderException("Order " + orderId + " cannot be cancelled in status " + order.getStatus());
        }
        order.transitionTo(OrderStatus.CANCELLED);

        // restore inventory (Rule 30)
        Restaurant r = restaurantService.get(order.getRestaurantId());
        if (r != null) {
            for (Map.Entry<String, Integer> e : order.getItems().entrySet()) {
                MenuItem m = r.getMenuItem(e.getKey());
                if (m != null) m.restoreStock(e.getValue());
            }
            r.releaseOrderSlot();
        }

        // refund (Rules 28, 29) - prepaid only, full refund here
        if (order.getPaymentMode() == PaymentMode.PREPAID && order.getPaymentId() != null) {
            Payment payment = paymentService.getById(order.getPaymentId());
            if (payment != null && payment.getStatus() == PaymentStatus.SUCCESS) {
                Customer c = customerService.get(order.getCustomerId());
                if (c != null) c.credit(order.getTotal(), "Refund for order " + orderId);
                paymentService.markRefunded(payment.getId());
                order.transitionTo(OrderStatus.REFUNDED);
                notificationService.notifyCustomer(order.getCustomerId(), orderId,
                        "Refund of Rs." + order.getTotal() + " credited to wallet");
            }
        }
    }

    private void audit(String message) {
        auditLog.add(System.currentTimeMillis() + " | " + message);
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    /** Top N selling items by quantity (Rule 49). */
    public List<Map.Entry<String, Integer>> topSellingItems(int n) {
        List<Map.Entry<String, Integer>> all = new ArrayList<>(itemSalesCount.entrySet());
        all.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        if (all.size() > n) return all.subList(0, n);
        return all;
    }
}

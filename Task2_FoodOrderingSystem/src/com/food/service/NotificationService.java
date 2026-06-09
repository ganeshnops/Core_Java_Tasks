package com.food.service;

/**
 * Simulated notifications.
 *  - Rule 36: customer order status notifications.
 *  - Rule 37: restaurant new order notifications.
 *  - Rule 38: delivery partner assignment notifications.
 *
 * In a real system these would be emails / SMS / push notifications.
 * Here we just print to console for demo.
 */
public class NotificationService {

    private static final boolean NOTIFY_ENABLED = true;

    public void notifyCustomer(String customerId, String orderId, String message) {
        if (NOTIFY_ENABLED) {
            System.out.println("  [notify customer " + customerId + ", order " + orderId + "] " + message);
        }
    }

    public void notifyRestaurant(String restaurantId, String orderId, String message) {
        if (NOTIFY_ENABLED) {
            System.out.println("  [notify restaurant " + restaurantId + ", order " + orderId + "] " + message);
        }
    }

    public void notifyDeliveryPartner(String partnerId, String orderId, String message) {
        if (NOTIFY_ENABLED) {
            System.out.println("  [notify rider " + partnerId + ", order " + orderId + "] " + message);
        }
    }
}

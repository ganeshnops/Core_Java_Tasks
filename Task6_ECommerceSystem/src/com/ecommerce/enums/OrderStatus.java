package com.ecommerce.enums;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Order state machine. (Rule Order 7)
 *  PENDING_PAYMENT -> CONFIRMED -> SHIPPED -> DELIVERED
 *               \\---> CANCELLED                    /
 *                       \\----> REFUNDED  <--------
 * Once SHIPPED, the order cannot be modified (Rule Order 4).
 */
public enum OrderStatus {
    PENDING_PAYMENT, CONFIRMED, SHIPPED, DELIVERED, CANCELLED, REFUNDED;

    private static final Map<OrderStatus, Set<OrderStatus>> NEXT = Map.of(
            PENDING_PAYMENT,  EnumSet.of(CONFIRMED, CANCELLED),
            CONFIRMED,        EnumSet.of(SHIPPED, CANCELLED),
            SHIPPED,          EnumSet.of(DELIVERED),
            DELIVERED,        EnumSet.noneOf(OrderStatus.class),
            CANCELLED,        EnumSet.of(REFUNDED),
            REFUNDED,         EnumSet.noneOf(OrderStatus.class)
    );

    public boolean canTransitionTo(OrderStatus to) {
        return NEXT.getOrDefault(this, EnumSet.noneOf(OrderStatus.class)).contains(to);
    }
}

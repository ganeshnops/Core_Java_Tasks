package com.food.enums;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Order lifecycle states.
 * Valid transitions are defined here so only correct status changes are allowed
 * (Rules 25 and 26).
 */
public enum OrderStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    PREPARING,
    READY,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED,
    REFUNDED;

    private static final Map<OrderStatus, Set<OrderStatus>> NEXT = Map.of(
            PENDING_PAYMENT,   EnumSet.of(CONFIRMED, CANCELLED),
            CONFIRMED,         EnumSet.of(PREPARING, CANCELLED),
            PREPARING,         EnumSet.of(READY, CANCELLED),
            READY,             EnumSet.of(OUT_FOR_DELIVERY),
            OUT_FOR_DELIVERY,  EnumSet.of(DELIVERED),
            DELIVERED,         EnumSet.noneOf(OrderStatus.class),
            CANCELLED,         EnumSet.of(REFUNDED),
            REFUNDED,          EnumSet.noneOf(OrderStatus.class)
    );

    /** True if 'from -> to' is a valid status transition. */
    public boolean canTransitionTo(OrderStatus to) {
        return NEXT.getOrDefault(this, EnumSet.noneOf(OrderStatus.class)).contains(to);
    }
}

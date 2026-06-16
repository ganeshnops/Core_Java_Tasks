package com.hotel.enums;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Booking lifecycle.
 * State machine:
 *   PENDING_PAYMENT -> CONFIRMED | CANCELLED
 *   CONFIRMED       -> CHECKED_IN | CANCELLED | NO_SHOW
 *   CHECKED_IN      -> CHECKED_OUT
 *   CANCELLED       -> REFUNDED
 *   CHECKED_OUT / NO_SHOW / REFUNDED  -> terminal
 */
public enum BookingStatus {

    PENDING_PAYMENT,
    CONFIRMED,
    CHECKED_IN,
    CHECKED_OUT,
    CANCELLED,
    NO_SHOW,
    REFUNDED;

    private static final Map<BookingStatus, Set<BookingStatus>> NEXT = Map.of(
            PENDING_PAYMENT, EnumSet.of(CONFIRMED, CANCELLED),
            CONFIRMED,       EnumSet.of(CHECKED_IN, CANCELLED, NO_SHOW),
            CHECKED_IN,      EnumSet.of(CHECKED_OUT),
            CHECKED_OUT,     EnumSet.noneOf(BookingStatus.class),
            CANCELLED,       EnumSet.of(REFUNDED),
            NO_SHOW,         EnumSet.noneOf(BookingStatus.class),
            REFUNDED,        EnumSet.noneOf(BookingStatus.class)
    );

    public boolean canTransitionTo(BookingStatus to) {
        return NEXT.getOrDefault(this, EnumSet.noneOf(BookingStatus.class)).contains(to);
    }

    /** True if this booking is currently holding the room (blocks new bookings). */
    public boolean blocksRoom() {
        return this == PENDING_PAYMENT || this == CONFIRMED || this == CHECKED_IN;
    }
}

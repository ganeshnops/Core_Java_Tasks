package com.booking.enums;

/** Seat category - determines base price multiplier. */
public enum SeatCategory {
    REGULAR(1.0),
    PREMIUM(1.5),
    RECLINER(2.0),
    VIP(3.0);

    private final double priceMultiplier;

    SeatCategory(double m) { this.priceMultiplier = m; }
    public double getPriceMultiplier() { return priceMultiplier; }
}

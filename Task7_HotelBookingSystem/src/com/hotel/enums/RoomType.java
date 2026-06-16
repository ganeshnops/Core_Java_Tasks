package com.hotel.enums;

/**
 * Room categories with their base (default) nightly tariff in INR.
 * Main.java uses RoomType.X.getBasePricePerNight() at bootstrap time.
 */
public enum RoomType {

    SINGLE(2000.0),
    DOUBLE(3500.0),
    DELUXE(5000.0),
    SUITE (8000.0);

    private final double basePricePerNight;

    RoomType(double basePricePerNight) {
        this.basePricePerNight = basePricePerNight;
    }

    public double getBasePricePerNight() {
        return basePricePerNight;
    }
}

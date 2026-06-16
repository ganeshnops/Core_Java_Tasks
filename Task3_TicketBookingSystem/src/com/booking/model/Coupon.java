package com.booking.model;

import java.time.LocalDate;

public class Coupon {

    private final String code;
    private final double discountPercent;
    private final double minBookingValue;
    private final LocalDate expiry;

    public Coupon(String code, double discountPercent, double minBookingValue, LocalDate expiry) {
        this.code = code;
        this.discountPercent = discountPercent;
        this.minBookingValue = minBookingValue;
        this.expiry = expiry;
    }

    public String getCode()             { return code; }
    public double getDiscountPercent()  { return discountPercent; }
    public double getMinBookingValue()  { return minBookingValue; }
    public LocalDate getExpiry()        { return expiry; }

    public boolean isExpired() {
        return LocalDate.now().isAfter(expiry);
    }

    public double calculateDiscount(double amount) {
        double discount = amount * discountPercent / 100.0;
        // Rule 5 of coupon - discount cannot exceed booking amount
        return Math.min(amount, discount);
    }

    @Override
    public String toString() {
        return String.format("%s | %.1f%% off | min Rs.%.2f | exp %s",
                code, discountPercent, minBookingValue, expiry);
    }
}

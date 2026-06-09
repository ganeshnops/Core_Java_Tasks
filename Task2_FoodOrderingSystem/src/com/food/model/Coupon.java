package com.food.model;

import java.time.LocalDate;

/** Discount coupon - Rules 15-18. */
public class Coupon {

    private final String code;
    private final double discountPercent;       // e.g. 10.0 for 10%
    private final double minOrderValue;
    private final LocalDate expiry;

    public Coupon(String code, double discountPercent, double minOrderValue, LocalDate expiry) {
        this.code = code;
        this.discountPercent = discountPercent;
        this.minOrderValue = minOrderValue;
        this.expiry = expiry;
    }

    public String getCode()             { return code; }
    public double getDiscountPercent()  { return discountPercent; }
    public double getMinOrderValue()    { return minOrderValue; }
    public LocalDate getExpiry()        { return expiry; }

    public boolean isExpired() {
        return LocalDate.now().isAfter(expiry);
    }

    public double calculateDiscount(double amount) {
        return Math.min(amount, amount * discountPercent / 100.0);
    }

    @Override
    public String toString() {
        return String.format("%s | %.1f%% off | min Rs.%.2f | expires %s",
                code, discountPercent, minOrderValue, expiry);
    }
}

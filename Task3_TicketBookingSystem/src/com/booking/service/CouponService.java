package com.booking.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.booking.exception.InvalidCouponException;
import com.booking.model.Coupon;

public class CouponService {

    private final Map<String, Coupon> coupons = new ConcurrentHashMap<>();

    public Coupon add(Coupon c) { coupons.put(c.getCode(), c); return c; }
    public Coupon get(String code) { return coupons.get(code); }

    public double validateAndCalculate(String code, double amount) {
        if (code == null || code.isBlank()) return 0;
        Coupon c = coupons.get(code);
        if (c == null)       throw new InvalidCouponException("Coupon not found: " + code);
        if (c.isExpired())   throw new InvalidCouponException("Coupon expired: " + code);
        if (amount < c.getMinBookingValue()) {
            throw new InvalidCouponException("Booking amount Rs." + amount
                    + " below coupon minimum Rs." + c.getMinBookingValue());
        }
        return c.calculateDiscount(amount);
    }
}

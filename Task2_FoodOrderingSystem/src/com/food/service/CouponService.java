package com.food.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.food.exception.InvalidCouponException;
import com.food.model.Coupon;

/**
 * Coupon storage + validation.
 * Rules:
 *   15 - coupon validation must happen before payment
 *   16 - only one coupon per order (enforced naturally - we validate ONE code)
 *   17 - expired coupons rejected
 *   18 - min order value validated
 */
public class CouponService {

    private final Map<String, Coupon> coupons = new ConcurrentHashMap<>();

    public Coupon add(Coupon c) { coupons.put(c.getCode(), c); return c; }
    public Coupon get(String code) { return coupons.get(code); }

    /**
     * Validate the coupon and return the discount amount.
     * Throws InvalidCouponException if the coupon is unknown, expired or
     * below the min order value.
     */
    public double validateAndCalculate(String code, double orderAmount) {
        if (code == null || code.isBlank()) return 0;
        Coupon c = coupons.get(code);
        if (c == null) {
            throw new InvalidCouponException("Coupon not found: " + code);
        }
        if (c.isExpired()) {
            throw new InvalidCouponException("Coupon expired: " + code);
        }
        if (orderAmount < c.getMinOrderValue()) {
            throw new InvalidCouponException("Order amount Rs." + orderAmount
                    + " is below the coupon minimum of Rs." + c.getMinOrderValue());
        }
        return c.calculateDiscount(orderAmount);
    }
}

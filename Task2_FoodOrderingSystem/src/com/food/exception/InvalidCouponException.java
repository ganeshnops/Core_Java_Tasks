package com.food.exception;

/** Rules 15-18 - coupon not found / expired / min value not met / multiple coupon. */
public class InvalidCouponException extends FoodOrderException {
    private static final long serialVersionUID = 1L;
    public InvalidCouponException(String message) { super(message); }
}

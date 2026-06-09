package com.food.exception;

/** Rules 22, 23, 59 - COD limit exceeded / payment failed / idempotency violation. */
public class PaymentFailedException extends FoodOrderException {
    private static final long serialVersionUID = 1L;
    public PaymentFailedException(String message) { super(message); }
}

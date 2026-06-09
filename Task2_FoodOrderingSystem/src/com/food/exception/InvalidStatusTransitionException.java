package com.food.exception;

/** Rules 25, 26 - invalid order status transition. */
public class InvalidStatusTransitionException extends FoodOrderException {
    private static final long serialVersionUID = 1L;
    public InvalidStatusTransitionException(String message) { super(message); }
}

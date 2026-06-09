package com.food.exception;

/**
 * Parent of all food-ordering business-rule exceptions.
 * Unchecked so callers can decide whether to handle.
 */
public class FoodOrderException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public FoodOrderException(String message) { super(message); }
}

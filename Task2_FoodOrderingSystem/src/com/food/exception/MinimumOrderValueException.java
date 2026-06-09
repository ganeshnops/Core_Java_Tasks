package com.food.exception;

/** Rule 4: Minimum order value must be met. */
public class MinimumOrderValueException extends FoodOrderException {
    private static final long serialVersionUID = 1L;
    public MinimumOrderValueException(String message) { super(message); }
}

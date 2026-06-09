package com.food.exception;

/** Rules 19, 60 - duplicate order detection. */
public class DuplicateOrderException extends FoodOrderException {
    private static final long serialVersionUID = 1L;
    public DuplicateOrderException(String message) { super(message); }
}

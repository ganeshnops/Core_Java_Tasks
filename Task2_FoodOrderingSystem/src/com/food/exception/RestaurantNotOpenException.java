package com.food.exception;

/** Rule 1: Restaurant must be OPEN to accept orders. (Also covers Rule 55 - SUSPENDED) */
public class RestaurantNotOpenException extends FoodOrderException {
    private static final long serialVersionUID = 1L;
    public RestaurantNotOpenException(String message) { super(message); }
}

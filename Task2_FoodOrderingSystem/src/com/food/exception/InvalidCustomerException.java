package com.food.exception;

/** Rules 5, 6, 53, 54 - customer must have a valid delivery address, verified mobile and not be blocked. */
public class InvalidCustomerException extends FoodOrderException {
    private static final long serialVersionUID = 1L;
    public InvalidCustomerException(String message) { super(message); }
}

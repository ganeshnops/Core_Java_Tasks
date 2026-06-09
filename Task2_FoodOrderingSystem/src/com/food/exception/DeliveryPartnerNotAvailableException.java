package com.food.exception;

/** Rules 32, 34, 56 - no delivery partner available / inactive / overloaded. */
public class DeliveryPartnerNotAvailableException extends FoodOrderException {
    private static final long serialVersionUID = 1L;
    public DeliveryPartnerNotAvailableException(String message) { super(message); }
}

package com.booking.exception;

public class InvalidCouponException extends BookingException {
    private static final long serialVersionUID = 1L;
    public InvalidCouponException(String message) { super(message); }
}

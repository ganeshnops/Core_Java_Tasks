package com.booking.exception;

/** Mobile / email not verified, etc. */
public class InvalidCustomerException extends BookingException {
    private static final long serialVersionUID = 1L;
    public InvalidCustomerException(String message) { super(message); }
}

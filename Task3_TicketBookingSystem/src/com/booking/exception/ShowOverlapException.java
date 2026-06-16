package com.booking.exception;

/** Two shows cannot overlap on the same screen. */
public class ShowOverlapException extends BookingException {
    private static final long serialVersionUID = 1L;
    public ShowOverlapException(String message) { super(message); }
}

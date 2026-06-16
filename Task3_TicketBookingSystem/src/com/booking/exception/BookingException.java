package com.booking.exception;

/** Parent exception for all booking system business errors. */
public class BookingException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public BookingException(String message) { super(message); }
}

package com.booking.exception;

/** Booking past the cancellation cut-off or already cancelled/completed. */
public class BookingNotCancellableException extends BookingException {
    private static final long serialVersionUID = 1L;
    public BookingNotCancellableException(String message) { super(message); }
}

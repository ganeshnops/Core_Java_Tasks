package com.booking.exception;

/** Seat is already booked / locked / blocked / disabled. */
public class SeatNotAvailableException extends BookingException {
    private static final long serialVersionUID = 1L;
    public SeatNotAvailableException(String message) { super(message); }
}

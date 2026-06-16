package com.booking.exception;

/** Show already started / cancelled / completed. */
public class ShowNotBookableException extends BookingException {
    private static final long serialVersionUID = 1L;
    public ShowNotBookableException(String message) { super(message); }
}

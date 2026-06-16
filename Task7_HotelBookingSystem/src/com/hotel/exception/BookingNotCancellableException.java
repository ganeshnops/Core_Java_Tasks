package com.hotel.exception;

public class BookingNotCancellableException extends HotelException {
    private static final long serialVersionUID = 1L;
    public BookingNotCancellableException(String m) { super(m); }
}

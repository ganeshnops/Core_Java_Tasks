package com.hotel.exception;

public class BookingConflictException extends HotelException {
    private static final long serialVersionUID = 1L;
    public BookingConflictException(String m) { super(m); }
}

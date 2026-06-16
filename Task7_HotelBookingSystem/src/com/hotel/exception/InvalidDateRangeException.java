package com.hotel.exception;

public class InvalidDateRangeException extends HotelException {
    private static final long serialVersionUID = 1L;
    public InvalidDateRangeException(String m) { super(m); }
}

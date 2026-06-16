package com.hotel.exception;

public class InvalidCustomerException extends HotelException {
    private static final long serialVersionUID = 1L;
    public InvalidCustomerException(String m) { super(m); }
}

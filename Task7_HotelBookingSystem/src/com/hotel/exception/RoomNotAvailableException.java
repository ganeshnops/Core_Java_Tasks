package com.hotel.exception;

public class RoomNotAvailableException extends HotelException {
    private static final long serialVersionUID = 1L;
    public RoomNotAvailableException(String m) { super(m); }
}

package com.hospital.exception;

public class UnauthorizedException extends HospitalException {
    private static final long serialVersionUID = 1L;
    public UnauthorizedException(String m) { super(m); }
}

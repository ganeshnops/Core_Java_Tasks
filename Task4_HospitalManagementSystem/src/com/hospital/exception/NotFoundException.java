package com.hospital.exception;

public class NotFoundException extends HospitalException {
    private static final long serialVersionUID = 1L;
    public NotFoundException(String m) { super(m); }
}

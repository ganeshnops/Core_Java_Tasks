package com.hospital.exception;

public class NoBedAvailableException extends HospitalException {
    private static final long serialVersionUID = 1L;
    public NoBedAvailableException(String m) { super(m); }
}

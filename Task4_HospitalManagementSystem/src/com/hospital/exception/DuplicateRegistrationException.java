package com.hospital.exception;

public class DuplicateRegistrationException extends HospitalException {
    private static final long serialVersionUID = 1L;
    public DuplicateRegistrationException(String m) { super(m); }
}

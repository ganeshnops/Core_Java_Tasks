package com.hospital.exception;

public class AppointmentConflictException extends HospitalException {
    private static final long serialVersionUID = 1L;
    public AppointmentConflictException(String m) { super(m); }
}

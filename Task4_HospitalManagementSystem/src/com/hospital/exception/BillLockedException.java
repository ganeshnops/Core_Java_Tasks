package com.hospital.exception;

public class BillLockedException extends HospitalException {
    private static final long serialVersionUID = 1L;
    public BillLockedException(String m) { super(m); }
}

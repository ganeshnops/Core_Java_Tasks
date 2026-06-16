package com.atm.exception;

public class InsufficientATMCashException extends ATMException {
    private static final long serialVersionUID = 1L;
    public InsufficientATMCashException(String m) { super(m); }
}

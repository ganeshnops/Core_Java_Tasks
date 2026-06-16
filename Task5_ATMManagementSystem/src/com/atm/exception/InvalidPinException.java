package com.atm.exception;

public class InvalidPinException extends ATMException {
    private static final long serialVersionUID = 1L;
    public InvalidPinException(String m) { super(m); }
}

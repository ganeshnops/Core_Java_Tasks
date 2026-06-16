package com.atm.exception;

public class SessionExpiredException extends ATMException {
    private static final long serialVersionUID = 1L;
    public SessionExpiredException(String m) { super(m); }
}

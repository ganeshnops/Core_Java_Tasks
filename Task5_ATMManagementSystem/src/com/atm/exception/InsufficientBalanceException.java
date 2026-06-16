package com.atm.exception;

public class InsufficientBalanceException extends ATMException {
    private static final long serialVersionUID = 1L;
    public InsufficientBalanceException(String m) { super(m); }
}

package com.atm.exception;

public class WithdrawalLimitException extends ATMException {
    private static final long serialVersionUID = 1L;
    public WithdrawalLimitException(String m) { super(m); }
}

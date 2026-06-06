package com.bank.exception;

/**
 * Thrown when the initial deposit while creating an account is below the
 * minimum required (Rule 1: at least Rs.1000).
 */
public class MinimumDepositException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MinimumDepositException(String message) {
        super(message);
    }
}

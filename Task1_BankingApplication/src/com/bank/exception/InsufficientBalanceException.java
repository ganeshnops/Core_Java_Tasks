package com.bank.exception;

/**
 * Thrown when a withdraw / transfer attempts to take more than the balance.
 * Covers Rule 4: Balance should never become negative.
 */
public class InsufficientBalanceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InsufficientBalanceException(String message) {
        super(message);
    }
}

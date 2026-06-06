package com.bank.exception;

/**
 * Thrown when a deposit / withdraw / transfer amount is <= 0.
 * Covers Rules: 2, 3, 5.
 */
public class InvalidAmountException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidAmountException(String message) {
        super(message);
    }
}

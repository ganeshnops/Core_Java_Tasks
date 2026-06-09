package com.food.exception;

/** Rule 43 - wallet balance not enough or going negative. */
public class WalletException extends FoodOrderException {
    private static final long serialVersionUID = 1L;
    public WalletException(String message) { super(message); }
}

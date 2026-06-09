	package com.food.exception;
	
	/** Rules 11, 12 - cart violations (different restaurant items, etc.). */
	public class CartException extends FoodOrderException {
	    private static final long serialVersionUID = 1L;
	    public CartException(String message) { super(message); }
	}

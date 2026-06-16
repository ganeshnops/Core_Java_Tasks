package com.ecommerce.exception;

public class InsufficientStockException extends ECommerceException {
    private static final long serialVersionUID = 1L;
    public InsufficientStockException(String m) { super(m); }
}

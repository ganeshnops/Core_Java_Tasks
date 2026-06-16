package com.ems.exception;

public class UnauthorizedException extends EMSException {
    private static final long serialVersionUID = 1L;
    public UnauthorizedException(String m) { super(m); }
}

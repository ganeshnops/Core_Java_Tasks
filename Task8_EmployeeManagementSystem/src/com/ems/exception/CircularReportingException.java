package com.ems.exception;

/** Thrown when assigning a manager would create a cycle in the reporting graph. */
public class CircularReportingException extends EMSException {
    private static final long serialVersionUID = 1L;
    public CircularReportingException(String m) { super(m); }
}

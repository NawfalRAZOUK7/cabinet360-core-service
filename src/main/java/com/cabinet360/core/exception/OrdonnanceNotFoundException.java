package com.cabinet360.core.exception;

public class OrdonnanceNotFoundException extends RuntimeException {
    public OrdonnanceNotFoundException(String message) {
        super(message);
    }

    public OrdonnanceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
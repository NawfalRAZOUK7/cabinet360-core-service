package com.cabinet360.core.exception;

public class NoteMedicaleNotFoundException extends RuntimeException {
    public NoteMedicaleNotFoundException(String message) {
        super(message);
    }

    public NoteMedicaleNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
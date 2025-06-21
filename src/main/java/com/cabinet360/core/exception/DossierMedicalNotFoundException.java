package com.cabinet360.core.exception;

public class DossierMedicalNotFoundException extends RuntimeException {
    public DossierMedicalNotFoundException(String message) {
        super(message);
    }

    public DossierMedicalNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
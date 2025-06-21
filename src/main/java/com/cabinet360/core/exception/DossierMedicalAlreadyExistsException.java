package com.cabinet360.core.exception;

public class DossierMedicalAlreadyExistsException extends RuntimeException {
    public DossierMedicalAlreadyExistsException(String message) {
        super(message);
    }

    public DossierMedicalAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
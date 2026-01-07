package com.redhat.healthcare.members.exception;

public class FhirValidationException extends RuntimeException {

    public FhirValidationException(String message) {
        super(message);
    }

    public FhirValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

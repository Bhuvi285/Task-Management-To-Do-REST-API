package com.taskmanager.exception;

public class CsrfOriginValidationException extends RuntimeException {

    public CsrfOriginValidationException(String message) {
        super(message);
    }
}

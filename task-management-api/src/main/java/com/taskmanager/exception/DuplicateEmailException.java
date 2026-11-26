package com.taskmanager.exception;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("Email is already registered: " + email);
    }

    public DuplicateEmailException(String email, Throwable cause) {
        super("Email is already registered: " + email, cause);
    }
}

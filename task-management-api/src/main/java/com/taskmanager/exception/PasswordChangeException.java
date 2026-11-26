package com.taskmanager.exception;

public class PasswordChangeException extends RuntimeException {

    public PasswordChangeException() {
        super("Current password is incorrect.");
    }
}

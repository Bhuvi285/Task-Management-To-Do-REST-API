package com.taskmanager.exception;

public class InvalidPasswordResetTokenException extends RuntimeException {

    public InvalidPasswordResetTokenException() {
        super("Password reset request is invalid or expired.");
    }
}

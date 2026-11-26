package com.taskmanager.exception;

public class RateLimitExceededException extends RuntimeException {

    private final String safeMessage;

    public RateLimitExceededException() {
        this("Too many login attempts. Please try again later.");
    }

    public RateLimitExceededException(String safeMessage) {
        super(safeMessage);
        this.safeMessage = safeMessage;
    }

    public String getSafeMessage() {
        return safeMessage;
    }
}

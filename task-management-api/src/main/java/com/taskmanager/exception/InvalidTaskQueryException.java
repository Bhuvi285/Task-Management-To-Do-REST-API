package com.taskmanager.exception;

public class InvalidTaskQueryException extends RuntimeException {

    public InvalidTaskQueryException(String message) {
        super(message);
    }
}

package com.petmatch.exception;

public class InvalidApplicationException extends RuntimeException {
    public InvalidApplicationException(String message) {
        super(message);
    }
}

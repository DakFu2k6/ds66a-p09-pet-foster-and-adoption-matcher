package com.petmatch.exception;

public class PlacementRejectedException extends RuntimeException {
    public PlacementRejectedException(String message) {
        super(message);
    }
}

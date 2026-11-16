package com.covoiturage.covoiturage_api.exception;

public class AccessDeniedActionException extends RuntimeException {
    public AccessDeniedActionException(String message) {
        super(message);
    }
}

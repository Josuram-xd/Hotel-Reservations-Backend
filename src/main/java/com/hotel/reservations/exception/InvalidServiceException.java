package com.hotel.reservations.exception;

/** Thrown when a requested extra service cannot be built. */
public class InvalidServiceException extends RuntimeException {

    public InvalidServiceException(String message) {
        super(message);
    }
}

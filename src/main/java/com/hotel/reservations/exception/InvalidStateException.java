package com.hotel.reservations.exception;

/** Thrown when an action is not allowed in the current reservation state. */
public class InvalidStateException extends RuntimeException {

    public InvalidStateException(String message) {
        super(message);
    }
}

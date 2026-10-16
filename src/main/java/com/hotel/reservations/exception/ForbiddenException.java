package com.hotel.reservations.exception;

/** Thrown when an authenticated user acts on a resource that belongs to someone else. */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}

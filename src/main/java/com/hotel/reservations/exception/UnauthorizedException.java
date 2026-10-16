package com.hotel.reservations.exception;

/** Thrown when a request has no valid session: missing, unknown or expired token. */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}

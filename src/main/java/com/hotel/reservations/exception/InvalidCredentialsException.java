package com.hotel.reservations.exception;

/** Thrown when the email or the password of a login attempt is wrong. */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}

package com.hotel.reservations.exception;

/** Thrown when registering with an email that already has an account. */
public class EmailAlreadyRegisteredException extends RuntimeException {

    public EmailAlreadyRegisteredException(String message) {
        super(message);
    }
}

package com.hotel.reservations.exception;

/** Thrown when a stay range is invalid, for example when checkOut is not after checkIn. */
public class InvalidDatesException extends RuntimeException {

    public InvalidDatesException(String message) {
        super(message);
    }
}

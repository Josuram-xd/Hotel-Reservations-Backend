package com.hotel.reservations.exception;

/** Thrown when a reservation id does not match any stored reservation. */
public class ReservationNotFoundException extends RuntimeException {

    public ReservationNotFoundException(String message) {
        super(message);
    }
}

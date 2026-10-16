package com.hotel.reservations.exception;

/** Thrown when a room already has a blocking reservation overlapping the requested dates. */
public class RoomNotAvailableException extends RuntimeException {

    public RoomNotAvailableException(String message) {
        super(message);
    }
}

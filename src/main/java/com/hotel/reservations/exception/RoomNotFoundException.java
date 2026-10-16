package com.hotel.reservations.exception;

/** Thrown when a room id does not match any room of the hotel. */
public class RoomNotFoundException extends RuntimeException {

    public RoomNotFoundException(String message) {
        super(message);
    }
}

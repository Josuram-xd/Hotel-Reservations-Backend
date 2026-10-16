package com.hotel.reservations.model;

/** Lifecycle of a reservation: CONFIRMED -> CHECKED_IN -> CHECKED_OUT. */
public enum ReservationStatus {
    CONFIRMED,
    CHECKED_IN,
    CHECKED_OUT
}

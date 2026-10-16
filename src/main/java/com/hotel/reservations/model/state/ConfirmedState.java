package com.hotel.reservations.model.state;

import com.hotel.reservations.model.ReservationStatus;

/** The reservation is booked and the guest has not arrived yet. */
public class ConfirmedState implements ReservationState {

    @Override
    public ReservationStatus status() {
        return ReservationStatus.CONFIRMED;
    }

    @Override
    public void ensureCanAddServices() {
        // allowed: extras can be booked in advance
    }

    @Override
    public ReservationState checkIn() {
        return new CheckedInState();
    }
}

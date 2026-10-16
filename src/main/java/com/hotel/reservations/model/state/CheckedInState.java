package com.hotel.reservations.model.state;

import com.hotel.reservations.model.ReservationStatus;

/** The guest is staying at the hotel and holds the digital key. */
public class CheckedInState implements ReservationState {

    @Override
    public ReservationStatus status() {
        return ReservationStatus.CHECKED_IN;
    }

    @Override
    public void ensureCanAddServices() {
        // allowed: extras can be added during the stay
    }

    @Override
    public ReservationState checkOut() {
        return new CheckedOutState();
    }

    @Override
    public void ensureCanAssignDigitalKey() {
        // allowed: the key is issued right after the check-in
    }
}

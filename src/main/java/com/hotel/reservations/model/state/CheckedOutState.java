package com.hotel.reservations.model.state;

import com.hotel.reservations.model.ReservationStatus;

/** The stay is over: the room is free again and only the final invoice can still be attached. */
public class CheckedOutState implements ReservationState {

    @Override
    public ReservationStatus status() {
        return ReservationStatus.CHECKED_OUT;
    }

    @Override
    public boolean blocksAvailability() {
        return false;
    }

    @Override
    public void ensureCanAttachInvoice() {
        // allowed: the invoice is generated right after the check-out
    }
}

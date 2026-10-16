package com.hotel.reservations.event;

import com.hotel.reservations.model.Reservation;

/** Published by the facade right after a reservation moves to CHECKED_IN. */
public record ReservationCheckedIn(Reservation reservation) {
}

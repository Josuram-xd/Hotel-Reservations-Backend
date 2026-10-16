package com.hotel.reservations.repository;

import com.hotel.reservations.model.Reservation;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** In-memory store of the reservations created at runtime. */
@Repository
public class ReservationRepository {

    private final List<Reservation> reservations = new ArrayList<>();

    public void save(Reservation reservation) {
        reservations.add(reservation);
    }

    public List<Reservation> findAll() {
        return List.copyOf(reservations);
    }

    public Optional<Reservation> findById(String id) {
        return reservations.stream()
                .filter(reservation -> reservation.getId().equals(id))
                .findFirst();
    }

    /** Reservations booked for the given room, in creation order. */
    public List<Reservation> findByRoom(int roomId) {
        return reservations.stream()
                .filter(reservation -> reservation.getRoom().getId() == roomId)
                .toList();
    }
}

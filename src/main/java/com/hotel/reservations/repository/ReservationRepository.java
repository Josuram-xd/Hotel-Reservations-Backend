package com.hotel.reservations.repository;

import com.hotel.reservations.model.Reservation;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory store of the reservations created at runtime.
 * Backed by a thread-safe list so availability reads stay safe while other
 * threads book, check in or check out.
 */
@Repository
public class ReservationRepository {

    private final List<Reservation> reservations = new CopyOnWriteArrayList<>();

    public void save(Reservation reservation) {
        reservations.add(reservation);
    }

    /** Removes every stored reservation. Used by tests to isolate scenarios. */
    public void deleteAll() {
        reservations.clear();
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

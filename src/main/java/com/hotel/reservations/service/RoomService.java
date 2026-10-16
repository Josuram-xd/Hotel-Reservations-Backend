package com.hotel.reservations.service;

import com.hotel.reservations.exception.InvalidDatesException;
import com.hotel.reservations.exception.RoomNotAvailableException;
import com.hotel.reservations.exception.RoomNotFoundException;
import com.hotel.reservations.model.Guest;
import com.hotel.reservations.model.Reservation;
import com.hotel.reservations.model.Room;
import com.hotel.reservations.model.RoomType;
import com.hotel.reservations.repository.ReservationRepository;
import com.hotel.reservations.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Availability of the rooms for a date range [checkIn, checkOut).
 * A room is available when it has no reservation that still blocks its period
 * and whose stay overlaps the requested range.
 */
@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    public RoomService(RoomRepository roomRepository, ReservationRepository reservationRepository) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
    }

    /** The room with the given id, or RoomNotFoundException when it does not exist. */
    public Room getRoom(int id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException("Room " + id + " was not found"));
    }

    /** Rooms of the given type (or every type when {@code type} is null) free for [checkIn, checkOut). */
    public List<Room> findAvailable(LocalDate checkIn, LocalDate checkOut, RoomType type) {
        validateRange(checkIn, checkOut);
        List<Room> candidates = (type == null) ? roomRepository.findAll() : roomRepository.findByType(type);
        return candidates.stream()
                .filter(room -> isAvailable(room, checkIn, checkOut))
                .toList();
    }

    /** True when the room has no blocking reservation overlapping [checkIn, checkOut). */
    public boolean isAvailable(Room room, LocalDate checkIn, LocalDate checkOut) {
        validateRange(checkIn, checkOut);
        return reservationRepository.findByRoom(room.getId()).stream()
                .filter(Reservation::blocksAvailability)
                .noneMatch(reservation -> reservation.overlaps(checkIn, checkOut));
    }

    /**
     * Reserves the room for [checkIn, checkOut). Fails when another blocking
     * reservation already overlaps the requested range.
     * Synchronized together with {@link #release}: the availability check and
     * the store happen atomically, so concurrent requests cannot double-book
     * the same room for overlapping dates.
     */
    public synchronized Reservation reserve(Room room, LocalDate checkIn, LocalDate checkOut, Guest guest) {
        return reserve(room, checkIn, checkOut, guest, null);
    }

    /** Same as {@link #reserve(Room, LocalDate, LocalDate, Guest)}, linking the reservation to its owner. */
    public synchronized Reservation reserve(Room room, LocalDate checkIn, LocalDate checkOut, Guest guest,
                                            String ownerId) {
        if (!isAvailable(room, checkIn, checkOut)) {
            throw new RoomNotAvailableException(
                    "Room " + room.getNumber() + " is not available for the requested dates");
        }
        Reservation reservation = new Reservation(room, guest, checkIn, checkOut, ownerId);
        reservationRepository.save(reservation);
        return reservation;
    }

    /**
     * Ends the stay and releases the room: CHECKED_IN -> CHECKED_OUT.
     * Synchronized together with {@link #reserve} to keep the reservation
     * lifecycle free of interleavings between simultaneous requests.
     */
    public synchronized void release(Reservation reservation) {
        reservation.checkOut();
    }

    private void validateRange(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new InvalidDatesException("checkIn and checkOut are required");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidDatesException("checkOut must be after checkIn");
        }
    }
}

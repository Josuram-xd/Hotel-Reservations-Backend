package com.hotel.reservations.service;

import com.hotel.reservations.exception.InvalidDatesException;
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

    private void validateRange(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new InvalidDatesException("checkIn and checkOut are required");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidDatesException("checkOut must be after checkIn");
        }
    }
}

package com.hotel.reservations.facade;

import com.hotel.reservations.model.Guest;
import com.hotel.reservations.model.Reservation;
import com.hotel.reservations.model.Room;
import com.hotel.reservations.service.RoomService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Single entry point for the hotel operations. Callers (the REST controller
 * and the tests) never talk to the individual services directly: the facade
 * orchestrates rooms, pricing, extras, billing and access.
 */
@Service
public class HotelFacade {

    private final RoomService roomService;

    public HotelFacade(RoomService roomService) {
        this.roomService = roomService;
    }

    /**
     * Creates a reservation for the given room and dates. Validates that the
     * room exists, that the dates form a valid range and that the room is
     * available for it.
     */
    public Reservation createReservation(int roomId, LocalDate checkIn, LocalDate checkOut, Guest guest) {
        Room room = roomService.getRoom(roomId);
        return roomService.reserve(room, checkIn, checkOut, guest);
    }
}

package com.hotel.reservations.facade;

import com.hotel.reservations.exception.ReservationNotFoundException;
import com.hotel.reservations.model.AdditionalService;
import com.hotel.reservations.model.Guest;
import com.hotel.reservations.model.Invoice;
import com.hotel.reservations.model.Reservation;
import com.hotel.reservations.model.Room;
import com.hotel.reservations.model.RoomType;
import com.hotel.reservations.model.ServiceType;
import com.hotel.reservations.repository.ReservationRepository;
import com.hotel.reservations.service.AccessService;
import com.hotel.reservations.service.AdditionalServiceService;
import com.hotel.reservations.service.BillingService;
import com.hotel.reservations.service.RoomService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Single entry point for the hotel operations. Callers (the REST controller
 * and the tests) never talk to the individual services directly: the facade
 * orchestrates rooms, pricing, extras, billing and access.
 */
@Service
public class HotelFacade {

    private final RoomService roomService;
    private final ReservationRepository reservationRepository;
    private final AdditionalServiceService additionalServiceService;
    private final BillingService billingService;
    private final AccessService accessService;

    public HotelFacade(RoomService roomService,
                       ReservationRepository reservationRepository,
                       AdditionalServiceService additionalServiceService,
                       BillingService billingService,
                       AccessService accessService) {
        this.roomService = roomService;
        this.reservationRepository = reservationRepository;
        this.additionalServiceService = additionalServiceService;
        this.billingService = billingService;
        this.accessService = accessService;
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

    /** Rooms of the given type (or every type when {@code type} is null) free for [checkIn, checkOut). */
    public List<Room> findAvailable(LocalDate checkIn, LocalDate checkOut, RoomType type) {
        return roomService.findAvailable(checkIn, checkOut, type);
    }

    /** The reservation with the given id, or ReservationNotFoundException when it does not exist. */
    public Reservation getReservation(String reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Reservation " + reservationId + " was not found"));
    }

    /** Adds an extra service to the reservation. Only allowed while CONFIRMED or CHECKED_IN. */
    public Reservation addService(String reservationId, ServiceType type) {
        Reservation reservation = getReservation(reservationId);
        AdditionalService service = additionalServiceService.create(type, reservation);
        reservation.addService(service);
        return reservation;
    }

    /** Checks the guest in (CONFIRMED -> CHECKED_IN) and issues the digital key. */
    public Reservation checkIn(String reservationId) {
        Reservation reservation = getReservation(reservationId);
        reservation.checkIn();
        reservation.assignDigitalKey(accessService.issueDigitalKey());
        return reservation;
    }

    /**
     * Checks the guest out (CHECKED_IN -> CHECKED_OUT): releases the room and
     * stores the final invoice with the room charges and the extra services.
     */
    public Reservation checkOut(String reservationId) {
        Reservation reservation = getReservation(reservationId);
        roomService.release(reservation);
        Invoice invoice = billingService.buildInvoice(reservation);
        reservation.attachInvoice(invoice);
        return reservation;
    }
}

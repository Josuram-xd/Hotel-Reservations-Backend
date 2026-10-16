package com.hotel.reservations.web;

import com.hotel.reservations.dto.AddServiceRequest;
import com.hotel.reservations.dto.AvailableRoomResponse;
import com.hotel.reservations.dto.CreateReservationRequest;
import com.hotel.reservations.dto.ReservationResponse;
import com.hotel.reservations.facade.HotelFacade;
import com.hotel.reservations.model.Guest;
import com.hotel.reservations.model.Reservation;
import com.hotel.reservations.model.RoomType;
import com.hotel.reservations.model.User;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** REST endpoints of the hotel, following the frontend API contract. */
@RestController
@RequestMapping("/api/hotel")
public class HotelController {

    private final HotelFacade facade;
    private final HotelMapper mapper;

    public HotelController(HotelFacade facade, HotelMapper mapper) {
        this.facade = facade;
        this.mapper = mapper;
    }

    /** Creates a reservation and returns it with 201. */
    @PostMapping("/reservar")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse createReservation(
            @RequestAttribute(AuthInterceptor.USER_ATTRIBUTE) User user,
            @Valid @RequestBody CreateReservationRequest request) {
        Guest guest = new Guest(
                request.guest().fullName(),
                request.guest().email(),
                request.guest().phone(),
                request.guest().document());
        Reservation reservation = facade.createReservation(
                user.id(), request.roomId(), request.checkIn(), request.checkOut(), guest);
        return mapper.toReservationResponse(reservation);
    }

    /** Availability for a date range, optionally filtered by room type, with the price preview. */
    @GetMapping("/disponibilidad")
    public List<AvailableRoomResponse> findAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false) RoomType type) {
        return facade.findAvailable(checkIn, checkOut, type).stream()
                .map(room -> mapper.toAvailableRoomResponse(room, checkIn, checkOut))
                .toList();
    }

    /** Adds an extra service to a reservation (only while CONFIRMED or CHECKED_IN). */
    @PostMapping("/servicios/{reservaId}")
    public ReservationResponse addService(@RequestAttribute(AuthInterceptor.USER_ATTRIBUTE) User user,
                                          @PathVariable String reservaId,
                                          @Valid @RequestBody AddServiceRequest request) {
        facade.getOwnedReservation(user.id(), reservaId);
        Reservation reservation = facade.addService(reservaId, request.serviceType());
        return mapper.toReservationResponse(reservation);
    }

    /** Checks the guest in; the returned reservation includes its digital key. */
    @PutMapping("/checkin/{reservaId}")
    public ReservationResponse checkIn(@RequestAttribute(AuthInterceptor.USER_ATTRIBUTE) User user,
                                       @PathVariable String reservaId) {
        facade.getOwnedReservation(user.id(), reservaId);
        return mapper.toReservationResponse(facade.checkIn(reservaId));
    }

    /** Checks the guest out; the returned reservation includes its final invoice. */
    @PutMapping("/checkout/{reservaId}")
    public ReservationResponse checkOut(@RequestAttribute(AuthInterceptor.USER_ATTRIBUTE) User user,
                                        @PathVariable String reservaId) {
        facade.getOwnedReservation(user.id(), reservaId);
        return mapper.toReservationResponse(facade.checkOut(reservaId));
    }

    /** Reservation details, including services, totals and invoice when available. */
    @GetMapping("/reserva/{reservaId}")
    public ReservationResponse getReservation(@RequestAttribute(AuthInterceptor.USER_ATTRIBUTE) User user,
                                              @PathVariable String reservaId) {
        return mapper.toReservationResponse(facade.getOwnedReservation(user.id(), reservaId));
    }
}

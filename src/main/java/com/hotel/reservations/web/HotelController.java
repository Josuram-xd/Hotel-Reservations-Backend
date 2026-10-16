package com.hotel.reservations.web;

import com.hotel.reservations.dto.CreateReservationRequest;
import com.hotel.reservations.dto.ReservationResponse;
import com.hotel.reservations.facade.HotelFacade;
import com.hotel.reservations.model.Guest;
import com.hotel.reservations.model.Reservation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
    public ReservationResponse createReservation(@Valid @RequestBody CreateReservationRequest request) {
        Guest guest = new Guest(
                request.guest().fullName(),
                request.guest().email(),
                request.guest().phone(),
                request.guest().document());
        Reservation reservation = facade.createReservation(
                request.roomId(), request.checkIn(), request.checkOut(), guest);
        return mapper.toReservationResponse(reservation);
    }
}

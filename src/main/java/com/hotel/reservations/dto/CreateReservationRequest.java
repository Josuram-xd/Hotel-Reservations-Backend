package com.hotel.reservations.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Body of POST /api/hotel/reservar. */
public record CreateReservationRequest(
        @NotNull(message = "La habitación es obligatoria") Integer roomId,
        @NotNull(message = "La fecha de entrada es obligatoria") LocalDate checkIn,
        @NotNull(message = "La fecha de salida es obligatoria") LocalDate checkOut,
        @NotNull(message = "Los datos del huésped son obligatorios") @Valid GuestRequest guest) {
}

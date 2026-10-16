package com.hotel.reservations.dto;

import com.hotel.reservations.model.ServiceType;
import jakarta.validation.constraints.NotNull;

/** Body of POST /api/hotel/servicios/{reservaId}. */
public record AddServiceRequest(
        @NotNull(message = "El servicio es obligatorio") ServiceType serviceType) {
}

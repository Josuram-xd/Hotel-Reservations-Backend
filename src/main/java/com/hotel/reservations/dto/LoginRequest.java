package com.hotel.reservations.dto;

import jakarta.validation.constraints.NotBlank;

/** Credentials sent to open a session. */
public record LoginRequest(
        @NotBlank(message = "El correo es obligatorio") String email,
        @NotBlank(message = "La contraseña es obligatoria") String password) {
}

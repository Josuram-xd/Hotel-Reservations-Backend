package com.hotel.reservations.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Guest data sent when creating a reservation. */
public record GuestRequest(
        @NotBlank(message = "El nombre completo es obligatorio") String fullName,
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es válido") String email,
        @NotBlank(message = "El teléfono es obligatorio") String phone,
        @NotBlank(message = "El documento es obligatorio") String document) {
}

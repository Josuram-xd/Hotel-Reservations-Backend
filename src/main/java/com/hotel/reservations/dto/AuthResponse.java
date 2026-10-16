package com.hotel.reservations.dto;

/** Returned by register and login: the bearer token and the user it belongs to. */
public record AuthResponse(String token, UserResponse user) {
}

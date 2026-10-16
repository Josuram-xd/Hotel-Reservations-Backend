package com.hotel.reservations.dto;

/** Guest data inside a reservation response. */
public record GuestResponse(String fullName, String email, String phone, String document) {
}

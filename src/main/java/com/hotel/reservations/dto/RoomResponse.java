package com.hotel.reservations.dto;

import com.hotel.reservations.model.RoomType;

/** Room data inside a reservation response. */
public record RoomResponse(int id, String number, RoomType type) {
}

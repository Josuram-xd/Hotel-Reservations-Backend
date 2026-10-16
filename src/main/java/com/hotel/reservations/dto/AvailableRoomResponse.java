package com.hotel.reservations.dto;

import com.hotel.reservations.model.RoomType;

import java.math.BigDecimal;

/** One room offered by GET /api/hotel/disponibilidad, with its price preview. */
public record AvailableRoomResponse(
        int id,
        String number,
        RoomType type,
        BigDecimal pricePerNight,
        int nights,
        int highSeasonNights,
        BigDecimal totalPrice) {
}

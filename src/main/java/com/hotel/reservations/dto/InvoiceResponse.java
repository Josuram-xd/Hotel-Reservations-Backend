package com.hotel.reservations.dto;

import com.hotel.reservations.model.RoomType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Invoice data inside a reservation response. */
public record InvoiceResponse(
        String number,
        String reservationId,
        String guestName,
        String roomNumber,
        RoomType roomType,
        LocalDate checkIn,
        LocalDate checkOut,
        int nights,
        List<InvoiceLineResponse> lines,
        BigDecimal subtotal,
        BigDecimal total,
        LocalDateTime issuedAt) {
}

package com.hotel.reservations.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/** Itemized invoice issued when a reservation is checked out. */
public record Invoice(
        String number,
        String reservationId,
        String guestName,
        String roomNumber,
        RoomType roomType,
        LocalDate checkIn,
        LocalDate checkOut,
        int nights,
        List<InvoiceLine> lines,
        BigDecimal subtotal,
        BigDecimal total,
        LocalDateTime issuedAt) {

    public Invoice {
        Objects.requireNonNull(number, "number must not be null");
        Objects.requireNonNull(reservationId, "reservationId must not be null");
        Objects.requireNonNull(guestName, "guestName must not be null");
        Objects.requireNonNull(roomNumber, "roomNumber must not be null");
        Objects.requireNonNull(roomType, "roomType must not be null");
        Objects.requireNonNull(checkIn, "checkIn must not be null");
        Objects.requireNonNull(checkOut, "checkOut must not be null");
        Objects.requireNonNull(subtotal, "subtotal must not be null");
        Objects.requireNonNull(total, "total must not be null");
        Objects.requireNonNull(issuedAt, "issuedAt must not be null");
        lines = List.copyOf(lines);
    }
}

package com.hotel.reservations.dto;

import com.hotel.reservations.model.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Full reservation payload returned by the API. */
public record ReservationResponse(
        String id,
        ReservationStatus status,
        RoomResponse room,
        GuestResponse guest,
        LocalDate checkIn,
        LocalDate checkOut,
        int nights,
        BigDecimal roomTotal,
        List<ServiceResponse> services,
        BigDecimal servicesTotal,
        BigDecimal total,
        String digitalKey,
        InvoiceResponse invoice) {
}

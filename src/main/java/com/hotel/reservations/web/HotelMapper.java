package com.hotel.reservations.web;

import com.hotel.reservations.dto.AvailableRoomResponse;
import com.hotel.reservations.dto.GuestResponse;
import com.hotel.reservations.dto.InvoiceLineResponse;
import com.hotel.reservations.dto.InvoiceResponse;
import com.hotel.reservations.dto.ReservationResponse;
import com.hotel.reservations.dto.RoomResponse;
import com.hotel.reservations.dto.ServiceResponse;
import com.hotel.reservations.model.Invoice;
import com.hotel.reservations.model.Reservation;
import com.hotel.reservations.model.Room;
import com.hotel.reservations.model.RoomType;
import com.hotel.reservations.service.RateService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Maps domain objects to the API DTOs defined in the frontend API contract. */
@Component
public class HotelMapper {

    private static final int MONEY_SCALE = 2;

    private final RateService rateService;

    public HotelMapper(RateService rateService) {
        this.rateService = rateService;
    }

    public ReservationResponse toReservationResponse(Reservation reservation) {
        RoomType type = reservation.getRoom().getType();
        BigDecimal roomTotal = rateService.totalForStay(type, reservation.getCheckIn(), reservation.getCheckOut());
        List<ServiceResponse> services = reservation.getServices().stream()
                .map(service -> new ServiceResponse(service.type(), service.name(), service.cost()))
                .toList();
        BigDecimal servicesTotal = services.stream()
                .map(ServiceResponse::cost)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        return new ReservationResponse(
                reservation.getId(),
                reservation.getStatus(),
                new RoomResponse(reservation.getRoom().getId(), reservation.getRoom().getNumber(), type),
                new GuestResponse(reservation.getGuest().fullName(), reservation.getGuest().email(),
                        reservation.getGuest().phone(), reservation.getGuest().document()),
                reservation.getCheckIn(),
                reservation.getCheckOut(),
                reservation.nights(),
                roomTotal,
                services,
                servicesTotal,
                roomTotal.add(servicesTotal),
                reservation.getDigitalKey(),
                reservation.getInvoice() == null ? null : toInvoiceResponse(reservation.getInvoice()));
    }

    /** Availability item: the room plus its price preview for the requested dates. */
    public AvailableRoomResponse toAvailableRoomResponse(Room room, LocalDate checkIn, LocalDate checkOut) {
        return new AvailableRoomResponse(
                room.getId(),
                room.getNumber(),
                room.getType(),
                rateService.basePrice(room.getType()),
                (int) ChronoUnit.DAYS.between(checkIn, checkOut),
                rateService.highSeasonNights(checkIn, checkOut),
                rateService.totalForStay(room.getType(), checkIn, checkOut));
    }

    public InvoiceResponse toInvoiceResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.number(),
                invoice.reservationId(),
                invoice.guestName(),
                invoice.roomNumber(),
                invoice.roomType(),
                invoice.checkIn(),
                invoice.checkOut(),
                invoice.nights(),
                invoice.lines().stream()
                        .map(line -> new InvoiceLineResponse(
                                line.description(), line.quantity(), line.unitPrice(), line.subtotal()))
                        .toList(),
                invoice.subtotal(),
                invoice.total(),
                invoice.issuedAt());
    }
}

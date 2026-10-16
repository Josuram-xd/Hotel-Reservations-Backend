package com.hotel.reservations.service;

import com.hotel.reservations.model.AdditionalService;
import com.hotel.reservations.model.Invoice;
import com.hotel.reservations.model.InvoiceLine;
import com.hotel.reservations.model.Reservation;
import com.hotel.reservations.model.RoomType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Builds the detailed invoice of a reservation: the room nights split by
 * season, one line per extra service, and the final subtotal and total.
 */
@Service
public class BillingService {

    private static final int MONEY_SCALE = 2;

    private final RateService rateService;
    private final AtomicInteger invoiceCounter = new AtomicInteger();

    public BillingService(RateService rateService) {
        this.rateService = rateService;
    }

    /** Builds (without storing) the itemized invoice of the reservation. */
    public Invoice buildInvoice(Reservation reservation) {
        Objects.requireNonNull(reservation, "reservation must not be null");

        RoomType type = reservation.getRoom().getType();
        int nights = reservation.nights();
        int highNights = rateService.highSeasonNights(reservation.getCheckIn(), reservation.getCheckOut());
        int lowNights = nights - highNights;

        List<InvoiceLine> lines = new ArrayList<>();
        if (lowNights > 0) {
            lines.add(line("Habitación " + type + " - noche de temporada baja",
                    lowNights, rateService.basePrice(type)));
        }
        if (highNights > 0) {
            lines.add(line("Habitación " + type + " - noche de temporada alta",
                    highNights, rateService.highSeasonPricePerNight(type)));
        }
        for (AdditionalService service : reservation.getServices()) {
            lines.add(line(service.name(), 1, service.cost()));
        }

        BigDecimal subtotal = lines.stream()
                .map(InvoiceLine::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        return new Invoice(
                nextInvoiceNumber(),
                reservation.getId(),
                reservation.getGuest().fullName(),
                reservation.getRoom().getNumber(),
                type,
                reservation.getCheckIn(),
                reservation.getCheckOut(),
                nights,
                lines,
                subtotal,
                subtotal,
                LocalDateTime.now());
    }

    private static InvoiceLine line(String description, int quantity, BigDecimal unitPrice) {
        BigDecimal subtotal = unitPrice
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        return new InvoiceLine(description, quantity, unitPrice, subtotal);
    }

    private String nextInvoiceNumber() {
        return "FAC-%04d".formatted(invoiceCounter.incrementAndGet());
    }
}

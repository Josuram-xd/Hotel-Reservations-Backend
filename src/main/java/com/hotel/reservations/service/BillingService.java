package com.hotel.reservations.service;

import com.hotel.reservations.model.AdditionalService;
import com.hotel.reservations.model.Invoice;
import com.hotel.reservations.model.Reservation;
import com.hotel.reservations.model.RoomType;
import com.hotel.reservations.service.billing.InvoiceBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Builds the detailed invoice of a reservation: the room nights split by
 * season, one line per extra service, and the final subtotal and total.
 * The invoice itself is assembled by an {@link InvoiceBuilder}.
 */
@Service
public class BillingService {

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

        InvoiceBuilder builder = InvoiceBuilder.forReservation(reservation)
                .number(nextInvoiceNumber())
                .issuedAt(LocalDateTime.now());
        if (lowNights > 0) {
            builder.addLine("Habitación " + type + " - noche de temporada baja",
                    lowNights, rateService.basePrice(type));
        }
        if (highNights > 0) {
            builder.addLine("Habitación " + type + " - noche de temporada alta",
                    highNights, rateService.highSeasonPricePerNight(type));
        }
        for (AdditionalService service : reservation.getServices()) {
            builder.addLine(service.name(), 1, service.cost());
        }
        return builder.build();
    }

    private String nextInvoiceNumber() {
        return "FAC-%04d".formatted(invoiceCounter.incrementAndGet());
    }
}

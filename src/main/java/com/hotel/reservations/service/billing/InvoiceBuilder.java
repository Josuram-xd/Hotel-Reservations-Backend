package com.hotel.reservations.service.billing;

import com.hotel.reservations.model.Invoice;
import com.hotel.reservations.model.InvoiceLine;
import com.hotel.reservations.model.Reservation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builder pattern: assembles the itemized invoice step by step. The reservation
 * data is copied once, lines are added one by one and the subtotal and total are
 * computed from them, so an invoice can never disagree with its own lines.
 */
public class InvoiceBuilder {

    private static final int MONEY_SCALE = 2;

    private final Reservation reservation;
    private final List<InvoiceLine> lines = new ArrayList<>();
    private String number;
    private LocalDateTime issuedAt;

    private InvoiceBuilder(Reservation reservation) {
        this.reservation = reservation;
    }

    /** Starts the invoice of a reservation: guest, room, dates and nights come from it. */
    public static InvoiceBuilder forReservation(Reservation reservation) {
        return new InvoiceBuilder(Objects.requireNonNull(reservation, "reservation must not be null"));
    }

    public InvoiceBuilder number(String number) {
        this.number = Objects.requireNonNull(number, "number must not be null");
        return this;
    }

    public InvoiceBuilder issuedAt(LocalDateTime issuedAt) {
        this.issuedAt = Objects.requireNonNull(issuedAt, "issuedAt must not be null");
        return this;
    }

    /** Adds a line whose subtotal is {@code unitPrice x quantity}. */
    public InvoiceBuilder addLine(String description, int quantity, BigDecimal unitPrice) {
        BigDecimal subtotal = unitPrice
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        lines.add(new InvoiceLine(description, quantity, unitPrice, subtotal));
        return this;
    }

    /** Builds the invoice. The number and the issue time are required and there must be at least one line. */
    public Invoice build() {
        if (number == null) {
            throw new IllegalStateException("The invoice number is required");
        }
        if (issuedAt == null) {
            throw new IllegalStateException("The issue time is required");
        }
        if (lines.isEmpty()) {
            throw new IllegalStateException("An invoice needs at least one line");
        }
        BigDecimal subtotal = lines.stream()
                .map(InvoiceLine::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        return new Invoice(
                number,
                reservation.getId(),
                reservation.getGuest().fullName(),
                reservation.getRoom().getNumber(),
                reservation.getRoom().getType(),
                reservation.getCheckIn(),
                reservation.getCheckOut(),
                reservation.nights(),
                lines,
                subtotal,
                subtotal,
                issuedAt);
    }
}

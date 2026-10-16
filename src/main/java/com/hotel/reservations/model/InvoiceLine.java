package com.hotel.reservations.model;

import java.math.BigDecimal;
import java.util.Objects;

/** One itemized line of an invoice: description, quantity, unit price and subtotal. */
public record InvoiceLine(String description, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {

    public InvoiceLine {
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(unitPrice, "unitPrice must not be null");
        Objects.requireNonNull(subtotal, "subtotal must not be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }
}

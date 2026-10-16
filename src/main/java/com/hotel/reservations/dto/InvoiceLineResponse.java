package com.hotel.reservations.dto;

import java.math.BigDecimal;

/** One itemized line inside an invoice response. */
public record InvoiceLineResponse(String description, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {
}

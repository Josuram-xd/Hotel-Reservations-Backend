package com.hotel.reservations.dto;

import com.hotel.reservations.model.ServiceType;

import java.math.BigDecimal;

/** One extra service inside a reservation response. */
public record ServiceResponse(ServiceType type, String name, BigDecimal cost) {
}

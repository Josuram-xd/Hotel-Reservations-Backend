package com.hotel.reservations.service;

import com.hotel.reservations.config.ExtrasProperties;
import com.hotel.reservations.model.AdditionalService;
import com.hotel.reservations.model.Reservation;
import com.hotel.reservations.model.ServiceType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Builds the extra services a reservation can include, priced from the
 * configured extras ({@code hotel.extras} in application.yml).
 */
@Service
public class AdditionalServiceService {

    private static final int MONEY_SCALE = 2;

    private final ExtrasProperties extras;

    public AdditionalServiceService(ExtrasProperties extras) {
        this.extras = extras;
    }

    /** Builds the extra service of the given type, priced for the reservation's stay length. */
    public AdditionalService create(ServiceType type, Reservation reservation) {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(reservation, "reservation must not be null");
        return switch (type) {
            case SPA -> new AdditionalService(type, "Spa", money(extras.spa()));
            case BREAKFAST -> new AdditionalService(type, "Desayuno",
                    money(extras.breakfastPerDay().multiply(BigDecimal.valueOf(reservation.nights()))));
            case TRANSFER -> new AdditionalService(type, "Traslado", money(extras.transfer()));
        };
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}

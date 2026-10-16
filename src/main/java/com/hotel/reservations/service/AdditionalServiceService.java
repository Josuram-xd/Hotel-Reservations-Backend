package com.hotel.reservations.service;

import com.hotel.reservations.config.ExtrasProperties;
import com.hotel.reservations.model.AdditionalService;
import com.hotel.reservations.model.Reservation;
import com.hotel.reservations.model.ServiceType;
import com.hotel.reservations.service.extras.ExtraServiceFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Builds the extra services a reservation can include, priced from the
 * configured extras ({@code hotel.extras} in application.yml). The creation
 * itself is delegated to the {@link ExtraServiceFactory}.
 */
@Service
public class AdditionalServiceService {

    private final ExtraServiceFactory factory;

    public AdditionalServiceService(ExtrasProperties extras) {
        this.factory = new ExtraServiceFactory(extras);
    }

    /** Builds the extra service of the given type, priced for the reservation's stay length. */
    public AdditionalService create(ServiceType type, Reservation reservation) {
        Objects.requireNonNull(reservation, "reservation must not be null");
        return factory.create(type, reservation.nights());
    }
}

package com.hotel.reservations.model;

import java.math.BigDecimal;
import java.util.Objects;

/** Extra service added to a reservation (spa, breakfast or transfer). Value object. */
public record AdditionalService(ServiceType type, String name, BigDecimal cost) {

    public AdditionalService {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(cost, "cost must not be null");
    }
}

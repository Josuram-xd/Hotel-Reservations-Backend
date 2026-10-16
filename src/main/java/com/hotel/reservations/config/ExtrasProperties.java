package com.hotel.reservations.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/** Prices of the extra services, configured in application.yml under {@code hotel.extras}. */
@ConfigurationProperties(prefix = "hotel.extras")
public record ExtrasProperties(BigDecimal spa, BigDecimal breakfastPerDay, BigDecimal transfer) {
}

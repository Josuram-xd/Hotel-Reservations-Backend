package com.hotel.reservations.service.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** High season: the night costs the base price multiplied by the configured multiplier (1.5 by default). */
public class HighSeasonStrategy implements PricingStrategy {

    private static final int MONEY_SCALE = 2;

    private final BigDecimal multiplier;

    public HighSeasonStrategy(BigDecimal multiplier) {
        this.multiplier = Objects.requireNonNull(multiplier, "multiplier must not be null");
    }

    @Override
    public BigDecimal priceFor(BigDecimal basePrice) {
        return basePrice.multiply(multiplier).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}

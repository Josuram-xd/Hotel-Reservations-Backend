package com.hotel.reservations.service.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Low season: the night costs the base price. */
public class LowSeasonStrategy implements PricingStrategy {

    private static final int MONEY_SCALE = 2;

    @Override
    public BigDecimal priceFor(BigDecimal basePrice) {
        return basePrice.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}

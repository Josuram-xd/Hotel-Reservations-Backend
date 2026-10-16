package com.hotel.reservations.service.pricing;

import java.math.BigDecimal;

/**
 * Strategy pattern: how the price of one night is derived from the base price
 * of the room type. Each season is its own strategy, so a new season rule is a
 * new class and not a new {@code if} inside the rate service.
 */
public interface PricingStrategy {

    /** The price of one night for a room whose base nightly price is {@code basePrice}. */
    BigDecimal priceFor(BigDecimal basePrice);
}

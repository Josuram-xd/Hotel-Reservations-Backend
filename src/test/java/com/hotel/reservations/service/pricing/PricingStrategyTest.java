package com.hotel.reservations.service.pricing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PricingStrategyTest {

    @Test
    void lowSeasonChargesTheBasePrice() {
        PricingStrategy strategy = new LowSeasonStrategy();

        assertThat(strategy.priceFor(new BigDecimal("90"))).isEqualByComparingTo("90.00");
        assertThat(strategy.priceFor(new BigDecimal("90")).scale()).isEqualTo(2);
    }

    @Test
    void highSeasonMultipliesTheBasePrice() {
        PricingStrategy strategy = new HighSeasonStrategy(new BigDecimal("1.5"));

        assertThat(strategy.priceFor(new BigDecimal("60.00"))).isEqualByComparingTo("90.00");
        assertThat(strategy.priceFor(new BigDecimal("180.00"))).isEqualByComparingTo("270.00");
    }

    @Test
    void highSeasonRoundsToTwoDecimalsHalfUp() {
        PricingStrategy strategy = new HighSeasonStrategy(new BigDecimal("1.5"));

        assertThat(strategy.priceFor(new BigDecimal("0.33"))).isEqualByComparingTo("0.50"); // 0.495 -> 0.50
    }

    @Test
    void strategiesAreInterchangeableBehindTheSameInterface() {
        BigDecimal base = new BigDecimal("100.00");
        PricingStrategy low = new LowSeasonStrategy();
        PricingStrategy high = new HighSeasonStrategy(new BigDecimal("2"));

        assertThat(high.priceFor(base)).isGreaterThan(low.priceFor(base));
    }

    @Test
    void highSeasonRequiresAMultiplier() {
        assertThatThrownBy(() -> new HighSeasonStrategy(null)).isInstanceOf(NullPointerException.class);
    }
}

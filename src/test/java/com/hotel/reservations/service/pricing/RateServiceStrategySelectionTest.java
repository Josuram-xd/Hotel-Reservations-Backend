package com.hotel.reservations.service.pricing;

import com.hotel.reservations.config.PricingProperties;
import com.hotel.reservations.config.PricingProperties.SeasonRange;
import com.hotel.reservations.model.RoomType;
import com.hotel.reservations.service.RateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** The rate service picks the pricing strategy of each night: high season inside the ranges, low season outside. */
class RateServiceStrategySelectionTest {

    private RateService rateService;

    @BeforeEach
    void setUp() {
        rateService = new RateService(new PricingProperties(
                Map.of(RoomType.SINGLE, new BigDecimal("60.00"),
                        RoomType.DOUBLE, new BigDecimal("90.00"),
                        RoomType.SUITE, new BigDecimal("180.00")),
                new BigDecimal("1.5"),
                List.of(new SeasonRange(MonthDay.of(6, 1), MonthDay.of(8, 31)),
                        new SeasonRange(MonthDay.of(12, 15), MonthDay.of(1, 6)))));
    }

    @Test
    void theLastLowSeasonNightAndTheFirstHighSeasonNightUseDifferentStrategies() {
        assertThat(rateService.pricePerNight(RoomType.DOUBLE, LocalDate.of(2026, 5, 31))).isEqualByComparingTo("90.00");
        assertThat(rateService.pricePerNight(RoomType.DOUBLE, LocalDate.of(2026, 6, 1))).isEqualByComparingTo("135.00");
    }

    @Test
    void theLastHighSeasonNightAndTheFirstLowSeasonNightUseDifferentStrategies() {
        assertThat(rateService.pricePerNight(RoomType.SUITE, LocalDate.of(2026, 8, 31))).isEqualByComparingTo("270.00");
        assertThat(rateService.pricePerNight(RoomType.SUITE, LocalDate.of(2026, 9, 1))).isEqualByComparingTo("180.00");
    }

    @Test
    void theRangeThatWrapsTheNewYearUsesTheHighSeasonStrategyOnBothSides() {
        assertThat(rateService.pricePerNight(RoomType.SINGLE, LocalDate.of(2026, 12, 31))).isEqualByComparingTo("90.00");
        assertThat(rateService.pricePerNight(RoomType.SINGLE, LocalDate.of(2027, 1, 2))).isEqualByComparingTo("90.00");
        assertThat(rateService.pricePerNight(RoomType.SINGLE, LocalDate.of(2027, 1, 7))).isEqualByComparingTo("60.00");
    }

    @Test
    void aStayCrossingTheSeasonMixesBothStrategiesNightByNight() {
        // Aug 30 and Aug 31 are high season, Sep 1 and Sep 2 are low season.
        BigDecimal total = rateService.totalForStay(RoomType.DOUBLE, LocalDate.of(2026, 8, 30), LocalDate.of(2026, 9, 3));

        assertThat(total).isEqualByComparingTo("450.00"); // 2 x 135.00 + 2 x 90.00
        assertThat(rateService.highSeasonPricePerNight(RoomType.DOUBLE)).isEqualByComparingTo("135.00");
    }
}

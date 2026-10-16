package com.hotel.reservations.config;

import com.hotel.reservations.model.RoomType;
import com.hotel.reservations.service.RateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.MonthDay;

import static org.assertj.core.api.Assertions.assertThat;

/** Verifies that the pricing rules in application.yml bind correctly. */
@SpringBootTest
class PricingConfigurationTest {

    @Autowired
    private PricingProperties properties;

    @Autowired
    private RateService rateService;

    @Test
    void bindsBasePricesPerRoomType() {
        assertThat(properties.basePrices().get(RoomType.SINGLE)).isEqualByComparingTo("60.00");
        assertThat(properties.basePrices().get(RoomType.DOUBLE)).isEqualByComparingTo("90.00");
        assertThat(properties.basePrices().get(RoomType.SUITE)).isEqualByComparingTo("180.00");
    }

    @Test
    void bindsTheHighSeasonMultiplierAndRanges() {
        assertThat(properties.highSeasonMultiplier()).isEqualByComparingTo("1.5");
        assertThat(properties.highSeason()).hasSize(2);
        assertThat(properties.highSeason().get(0).start()).isEqualTo(MonthDay.of(6, 1));
        assertThat(properties.highSeason().get(0).end()).isEqualTo(MonthDay.of(8, 31));
        assertThat(properties.highSeason().get(1).start()).isEqualTo(MonthDay.of(12, 15));
        assertThat(properties.highSeason().get(1).end()).isEqualTo(MonthDay.of(1, 6));
    }

    @Test
    void pricesAHighSeasonStayWithTheConfiguredValues() {
        // 2 summer nights in a DOUBLE room: 2 x 90.00 x 1.5 = 270.00
        assertThat(rateService.totalForStay(RoomType.DOUBLE, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 3)))
                .isEqualByComparingTo("270.00");
    }
}

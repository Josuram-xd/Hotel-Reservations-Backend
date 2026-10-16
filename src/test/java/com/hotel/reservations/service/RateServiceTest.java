package com.hotel.reservations.service;

import com.hotel.reservations.config.PricingProperties;
import com.hotel.reservations.config.PricingProperties.SeasonRange;
import com.hotel.reservations.exception.InvalidDatesException;
import com.hotel.reservations.model.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RateServiceTest {

    private RateService rateService;

    @BeforeEach
    void setUp() {
        PricingProperties properties = new PricingProperties(
                Map.of(
                        RoomType.SINGLE, new BigDecimal("60.00"),
                        RoomType.DOUBLE, new BigDecimal("90.00"),
                        RoomType.SUITE, new BigDecimal("180.00")),
                new BigDecimal("1.5"),
                List.of(
                        new SeasonRange(MonthDay.of(6, 1), MonthDay.of(8, 31)),
                        new SeasonRange(MonthDay.of(12, 15), MonthDay.of(1, 6))));
        rateService = new RateService(properties);
    }

    @Test
    void lowSeasonNightsUseTheBasePrice() {
        LocalDate checkIn = LocalDate.of(2026, 11, 1);
        LocalDate checkOut = LocalDate.of(2026, 11, 5);

        assertThat(rateService.pricePerNight(RoomType.DOUBLE, checkIn)).isEqualByComparingTo("90.00");
        assertThat(rateService.highSeasonNights(checkIn, checkOut)).isZero();
        assertThat(rateService.totalForStay(RoomType.DOUBLE, checkIn, checkOut)).isEqualByComparingTo("360.00");
    }

    @Test
    void highSeasonNightsApplyTheMultiplier() {
        LocalDate checkIn = LocalDate.of(2026, 7, 1);
        LocalDate checkOut = LocalDate.of(2026, 7, 4);

        assertThat(rateService.pricePerNight(RoomType.DOUBLE, checkIn)).isEqualByComparingTo("135.00");
        assertThat(rateService.highSeasonNights(checkIn, checkOut)).isEqualTo(3);
        assertThat(rateService.totalForStay(RoomType.DOUBLE, checkIn, checkOut)).isEqualByComparingTo("405.00");
    }

    @Test
    void mixedSeasonStayIsChargedNightByNight() {
        LocalDate checkIn = LocalDate.of(2026, 12, 30);
        LocalDate checkOut = LocalDate.of(2027, 1, 8);

        // High nights: Dec 30, Dec 31 and Jan 1-6 (8). Low nights: Jan 7 (1).
        assertThat(rateService.highSeasonNights(checkIn, checkOut)).isEqualTo(8);
        // 8 x 90.00 (high) + 1 x 60.00 (low)
        assertThat(rateService.totalForStay(RoomType.SINGLE, checkIn, checkOut)).isEqualByComparingTo("780.00");
    }

    @Test
    void seasonBoundariesAreInclusive() {
        assertThat(rateService.isHighSeason(LocalDate.of(2026, 5, 31))).isFalse();
        assertThat(rateService.isHighSeason(LocalDate.of(2026, 6, 1))).isTrue();
        assertThat(rateService.isHighSeason(LocalDate.of(2026, 8, 31))).isTrue();
        assertThat(rateService.isHighSeason(LocalDate.of(2026, 9, 1))).isFalse();
        assertThat(rateService.isHighSeason(LocalDate.of(2026, 12, 15))).isTrue();
        assertThat(rateService.isHighSeason(LocalDate.of(2027, 1, 6))).isTrue();
        assertThat(rateService.isHighSeason(LocalDate.of(2027, 1, 7))).isFalse();
    }

    @Test
    void suiteUsesItsOwnBasePriceInHighSeason() {
        assertThat(rateService.pricePerNight(RoomType.SUITE, LocalDate.of(2026, 12, 25)))
                .isEqualByComparingTo("270.00");
    }

    @Test
    void singleNightStayIsPricedAsOneNight() {
        LocalDate night = LocalDate.of(2026, 11, 1);

        assertThat(rateService.totalForStay(RoomType.SINGLE, night, night.plusDays(1)))
                .isEqualByComparingTo("60.00");
    }

    @Test
    void rejectsInvalidRanges() {
        LocalDate date = LocalDate.of(2026, 11, 1);

        assertThatThrownBy(() -> rateService.totalForStay(RoomType.DOUBLE, date, date))
                .isInstanceOf(InvalidDatesException.class);
        assertThatThrownBy(() -> rateService.totalForStay(RoomType.DOUBLE, date, null))
                .isInstanceOf(InvalidDatesException.class);
        assertThatThrownBy(() -> rateService.highSeasonNights(date, date))
                .isInstanceOf(InvalidDatesException.class);
    }
}

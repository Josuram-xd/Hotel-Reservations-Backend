package com.hotel.reservations.config;

import com.hotel.reservations.model.RoomType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.MonthDay;
import java.util.List;
import java.util.Map;

/**
 * Pricing rules configured in application.yml under {@code hotel.pricing}:
 * base price per room type, the high-season multiplier and the high-season
 * ranges (month-day precision, evaluated night by night).
 */
@ConfigurationProperties(prefix = "hotel.pricing")
public record PricingProperties(
        Map<RoomType, BigDecimal> basePrices,
        BigDecimal highSeasonMultiplier,
        List<SeasonRange> highSeason) {

    /** A high-season window; when start is after end it wraps around the new year. */
    public record SeasonRange(
            @DateTimeFormat(pattern = "MM-dd") MonthDay start,
            @DateTimeFormat(pattern = "MM-dd") MonthDay end) {
    }
}

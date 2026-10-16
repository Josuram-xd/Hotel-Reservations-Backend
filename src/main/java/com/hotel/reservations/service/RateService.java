package com.hotel.reservations.service;

import com.hotel.reservations.config.PricingProperties;
import com.hotel.reservations.exception.InvalidDatesException;
import com.hotel.reservations.model.RoomType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.MonthDay;

/**
 * Prices a stay. The season is evaluated night by night, so a stay that
 * crosses the high season is charged with the multiplier only for the nights
 * that actually fall inside a high-season range.
 */
@Service
public class RateService {

    private static final int MONEY_SCALE = 2;

    private final PricingProperties prices;

    public RateService(PricingProperties prices) {
        this.prices = prices;
    }

    /** Base nightly price configured for the room type (no season applied). */
    public BigDecimal basePrice(RoomType type) {
        BigDecimal base = prices.basePrices().get(type);
        if (base == null) {
            throw new IllegalArgumentException("No base price configured for room type " + type);
        }
        return base.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    /** Price of one night: the base price, multiplied when the night falls in high season. */
    public BigDecimal pricePerNight(RoomType type, LocalDate date) {
        return isHighSeason(date) ? highSeasonPricePerNight(type) : basePrice(type);
    }

    /** Price of one high-season night for the room type (base price x multiplier). */
    public BigDecimal highSeasonPricePerNight(RoomType type) {
        return basePrice(type)
                .multiply(prices.highSeasonMultiplier())
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    /** Number of nights of [checkIn, checkOut) that fall in high season. */
    public int highSeasonNights(LocalDate checkIn, LocalDate checkOut) {
        validateRange(checkIn, checkOut);
        int count = 0;
        for (LocalDate night = checkIn; night.isBefore(checkOut); night = night.plusDays(1)) {
            if (isHighSeason(night)) {
                count++;
            }
        }
        return count;
    }

    /** Total price of the stay: the price of every night of [checkIn, checkOut), added up. */
    public BigDecimal totalForStay(RoomType type, LocalDate checkIn, LocalDate checkOut) {
        validateRange(checkIn, checkOut);
        BigDecimal total = BigDecimal.ZERO;
        for (LocalDate night = checkIn; night.isBefore(checkOut); night = night.plusDays(1)) {
            total = total.add(pricePerNight(type, night));
        }
        return total.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    /** True when the date falls inside any of the configured high-season ranges. */
    public boolean isHighSeason(LocalDate date) {
        MonthDay monthDay = MonthDay.from(date);
        return prices.highSeason().stream().anyMatch(range -> contains(range, monthDay));
    }

    private static boolean contains(PricingProperties.SeasonRange range, MonthDay monthDay) {
        if (range.start().isAfter(range.end())) {
            // The range wraps around the new year, e.g. Dec 15 - Jan 6.
            return !monthDay.isBefore(range.start()) || !monthDay.isAfter(range.end());
        }
        return !monthDay.isBefore(range.start()) && !monthDay.isAfter(range.end());
    }

    private void validateRange(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new InvalidDatesException("checkIn and checkOut are required");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidDatesException("checkOut must be after checkIn");
        }
    }
}

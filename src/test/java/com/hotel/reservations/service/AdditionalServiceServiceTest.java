package com.hotel.reservations.service;

import com.hotel.reservations.config.ExtrasProperties;
import com.hotel.reservations.model.AdditionalService;
import com.hotel.reservations.model.Guest;
import com.hotel.reservations.model.Reservation;
import com.hotel.reservations.model.Room;
import com.hotel.reservations.model.RoomType;
import com.hotel.reservations.model.ServiceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class AdditionalServiceServiceTest {

    private static final Guest GUEST =
            new Guest("Ada Lovelace", "ada@example.com", "3001234567", "CC-100");

    private AdditionalServiceService additionalServiceService;
    private Reservation reservation; // 4 nights

    @BeforeEach
    void setUp() {
        ExtrasProperties extras = new ExtrasProperties(
                new BigDecimal("50.00"), new BigDecimal("15.00"), new BigDecimal("30.00"));
        additionalServiceService = new AdditionalServiceService(extras);
        reservation = new Reservation(new Room(6, "201", RoomType.DOUBLE), GUEST,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 5));
    }

    @Test
    void spaHasAFlatPrice() {
        AdditionalService spa = additionalServiceService.create(ServiceType.SPA, reservation);

        assertThat(spa.type()).isEqualTo(ServiceType.SPA);
        assertThat(spa.name()).isEqualTo("Spa");
        assertThat(spa.cost()).isEqualByComparingTo("50.00");
    }

    @Test
    void breakfastIsChargedPerNightOfStay() {
        AdditionalService breakfast = additionalServiceService.create(ServiceType.BREAKFAST, reservation);

        assertThat(breakfast.type()).isEqualTo(ServiceType.BREAKFAST);
        assertThat(breakfast.name()).isEqualTo("Desayuno");
        assertThat(breakfast.cost()).isEqualByComparingTo("60.00"); // 4 nights x 15.00
    }

    @Test
    void transferHasAFlatPrice() {
        AdditionalService transfer = additionalServiceService.create(ServiceType.TRANSFER, reservation);

        assertThat(transfer.name()).isEqualTo("Traslado");
        assertThat(transfer.cost()).isEqualByComparingTo("30.00");
    }
}

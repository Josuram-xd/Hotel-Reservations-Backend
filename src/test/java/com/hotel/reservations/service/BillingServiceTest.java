package com.hotel.reservations.service;

import com.hotel.reservations.config.PricingProperties;
import com.hotel.reservations.config.PricingProperties.SeasonRange;
import com.hotel.reservations.exception.InvalidStateException;
import com.hotel.reservations.model.AdditionalService;
import com.hotel.reservations.model.Guest;
import com.hotel.reservations.model.Invoice;
import com.hotel.reservations.model.Reservation;
import com.hotel.reservations.model.Room;
import com.hotel.reservations.model.RoomType;
import com.hotel.reservations.model.ServiceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BillingServiceTest {

    private static final Guest GUEST =
            new Guest("Ada Lovelace", "ada@example.com", "3001234567", "CC-100");
    private static final Room DOUBLE_201 = new Room(6, "201", RoomType.DOUBLE);
    private static final Room SINGLE_101 = new Room(1, "101", RoomType.SINGLE);

    private BillingService billingService;

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
        billingService = new BillingService(new RateService(properties));
    }

    @Test
    void lowSeasonStayProducesASingleRoomLine() {
        Reservation reservation = new Reservation(DOUBLE_201, GUEST,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 5));

        Invoice invoice = billingService.buildInvoice(reservation);

        assertThat(invoice.number()).startsWith("FAC-");
        assertThat(invoice.reservationId()).isEqualTo(reservation.getId());
        assertThat(invoice.guestName()).isEqualTo("Ada Lovelace");
        assertThat(invoice.roomNumber()).isEqualTo("201");
        assertThat(invoice.roomType()).isEqualTo(RoomType.DOUBLE);
        assertThat(invoice.nights()).isEqualTo(4);
        assertThat(invoice.issuedAt()).isNotNull();
        assertThat(invoice.lines()).hasSize(1);
        assertThat(invoice.lines().get(0).description()).contains("temporada baja");
        assertThat(invoice.lines().get(0).quantity()).isEqualTo(4);
        assertThat(invoice.lines().get(0).unitPrice()).isEqualByComparingTo("90.00");
        assertThat(invoice.lines().get(0).subtotal()).isEqualByComparingTo("360.00");
        assertThat(invoice.subtotal()).isEqualByComparingTo("360.00");
        assertThat(invoice.total()).isEqualByComparingTo("360.00");
    }

    @Test
    void mixedSeasonStaySplitsTheRoomLineBySeason() {
        Reservation reservation = new Reservation(SINGLE_101, GUEST,
                LocalDate.of(2026, 12, 30), LocalDate.of(2027, 1, 8));

        Invoice invoice = billingService.buildInvoice(reservation);

        assertThat(invoice.lines()).hasSize(2);
        assertThat(invoice.lines().get(0).description()).contains("temporada baja");
        assertThat(invoice.lines().get(0).quantity()).isEqualTo(1);
        assertThat(invoice.lines().get(0).unitPrice()).isEqualByComparingTo("60.00");
        assertThat(invoice.lines().get(1).description()).contains("temporada alta");
        assertThat(invoice.lines().get(1).quantity()).isEqualTo(8);
        assertThat(invoice.lines().get(1).unitPrice()).isEqualByComparingTo("90.00");
        assertThat(invoice.subtotal()).isEqualByComparingTo("780.00");
    }

    @Test
    void extraServicesAppearAsTheirOwnLines() {
        Reservation reservation = new Reservation(DOUBLE_201, GUEST,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3)); // 2 nights
        reservation.addService(new AdditionalService(ServiceType.SPA, "Spa", new BigDecimal("50.00")));
        reservation.addService(new AdditionalService(ServiceType.BREAKFAST, "Desayuno", new BigDecimal("30.00")));

        Invoice invoice = billingService.buildInvoice(reservation);

        assertThat(invoice.lines()).hasSize(3);
        assertThat(invoice.lines().get(1).description()).isEqualTo("Spa");
        assertThat(invoice.lines().get(1).subtotal()).isEqualByComparingTo("50.00");
        assertThat(invoice.lines().get(2).description()).isEqualTo("Desayuno");
        assertThat(invoice.lines().get(2).subtotal()).isEqualByComparingTo("30.00");
        assertThat(invoice.total()).isEqualByComparingTo("260.00"); // 180 + 50 + 30
    }

    @Test
    void invoiceNumbersAreSequential() {
        Reservation first = new Reservation(DOUBLE_201, GUEST,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3));
        Reservation second = new Reservation(SINGLE_101, GUEST,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3));

        Invoice firstInvoice = billingService.buildInvoice(first);
        Invoice secondInvoice = billingService.buildInvoice(second);

        assertThat(firstInvoice.number()).isEqualTo("FAC-0001");
        assertThat(secondInvoice.number()).isEqualTo("FAC-0002");
    }

    @Test
    void invoiceIsOnlyAttachedOnceAndOnlyAfterCheckOut() {
        Reservation reservation = new Reservation(DOUBLE_201, GUEST,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3));
        Invoice invoice = billingService.buildInvoice(reservation);

        assertThatThrownBy(() -> reservation.attachInvoice(invoice))
                .isInstanceOf(InvalidStateException.class);

        reservation.checkIn();
        reservation.checkOut();
        reservation.attachInvoice(invoice);

        assertThat(reservation.getInvoice()).isEqualTo(invoice);
        assertThatThrownBy(() -> reservation.attachInvoice(invoice))
                .isInstanceOf(InvalidStateException.class);
    }
}

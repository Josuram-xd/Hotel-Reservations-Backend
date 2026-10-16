package com.hotel.reservations.event;

import com.hotel.reservations.config.PricingProperties;
import com.hotel.reservations.config.PricingProperties.SeasonRange;
import com.hotel.reservations.exception.InvalidStateException;
import com.hotel.reservations.model.Guest;
import com.hotel.reservations.model.Reservation;
import com.hotel.reservations.model.ReservationStatus;
import com.hotel.reservations.model.Room;
import com.hotel.reservations.model.RoomType;
import com.hotel.reservations.service.AccessService;
import com.hotel.reservations.service.BillingService;
import com.hotel.reservations.service.RateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Each listener reacts to its own event and to nothing else. */
class ReservationListenersTest {

    private Reservation reservation;
    private DigitalKeyListener keyListener;
    private InvoiceListener invoiceListener;

    @BeforeEach
    void setUp() {
        reservation = new Reservation(new Room(6, "201", RoomType.DOUBLE),
                new Guest("Ada Lovelace", "ada@example.com", "3001234567", "CC-100"),
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3));
        RateService rateService = new RateService(new PricingProperties(
                Map.of(RoomType.SINGLE, new BigDecimal("60.00"),
                        RoomType.DOUBLE, new BigDecimal("90.00"),
                        RoomType.SUITE, new BigDecimal("180.00")),
                new BigDecimal("1.5"),
                List.of(new SeasonRange(MonthDay.of(6, 1), MonthDay.of(8, 31)))));
        keyListener = new DigitalKeyListener(new AccessService());
        invoiceListener = new InvoiceListener(new BillingService(rateService));
    }

    @Test
    void theKeyListenerIssuesTheDigitalKeyAfterTheCheckIn() {
        reservation.checkIn();

        keyListener.onCheckedIn(new ReservationCheckedIn(reservation));

        assertThat(reservation.getDigitalKey()).matches("[0-9a-f-]{36}");
    }

    @Test
    void theKeyListenerFailsWhenTheReservationIsNotCheckedIn() {
        assertThatThrownBy(() -> keyListener.onCheckedIn(new ReservationCheckedIn(reservation)))
                .isInstanceOf(InvalidStateException.class);
        assertThat(reservation.getDigitalKey()).isNull();
    }

    @Test
    void theInvoiceListenerAttachesTheFinalInvoiceAfterTheCheckOut() {
        reservation.checkIn();
        reservation.checkOut();

        invoiceListener.onCheckedOut(new ReservationCheckedOut(reservation));

        assertThat(reservation.getInvoice()).isNotNull();
        assertThat(reservation.getInvoice().number()).startsWith("FAC-");
        assertThat(reservation.getInvoice().total()).isEqualByComparingTo("180.00"); // 2 nights x 90.00
    }

    @Test
    void theInvoiceListenerFailsBeforeTheCheckOut() {
        assertThatThrownBy(() -> invoiceListener.onCheckedOut(new ReservationCheckedOut(reservation)))
                .isInstanceOf(InvalidStateException.class);
        assertThat(reservation.getInvoice()).isNull();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void theAuditLoggerObservesBothEventsWithoutChangingTheReservation() {
        ReservationAuditLogger logger = new ReservationAuditLogger();

        assertThatCode(() -> {
            logger.onCheckedIn(new ReservationCheckedIn(reservation));
            logger.onCheckedOut(new ReservationCheckedOut(reservation));
        }).doesNotThrowAnyException();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }
}

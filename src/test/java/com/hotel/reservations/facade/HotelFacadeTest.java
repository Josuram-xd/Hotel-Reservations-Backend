package com.hotel.reservations.facade;

import com.hotel.reservations.config.ExtrasProperties;
import com.hotel.reservations.config.PricingProperties;
import com.hotel.reservations.config.PricingProperties.SeasonRange;
import com.hotel.reservations.event.DigitalKeyListener;
import com.hotel.reservations.event.InvoiceListener;
import com.hotel.reservations.event.ReservationCheckedIn;
import com.hotel.reservations.event.ReservationCheckedOut;
import com.hotel.reservations.exception.ForbiddenException;
import com.hotel.reservations.exception.InvalidDatesException;
import com.hotel.reservations.exception.InvalidStateException;
import com.hotel.reservations.exception.ReservationNotFoundException;
import com.hotel.reservations.exception.RoomNotAvailableException;
import com.hotel.reservations.exception.RoomNotFoundException;
import com.hotel.reservations.model.Guest;
import com.hotel.reservations.model.Reservation;
import com.hotel.reservations.model.ReservationStatus;
import com.hotel.reservations.model.RoomType;
import com.hotel.reservations.model.ServiceType;
import com.hotel.reservations.repository.ReservationRepository;
import com.hotel.reservations.repository.RoomRepository;
import com.hotel.reservations.service.AccessService;
import com.hotel.reservations.service.AdditionalServiceService;
import com.hotel.reservations.service.BillingService;
import com.hotel.reservations.service.RateService;
import com.hotel.reservations.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HotelFacadeTest {

    private static final Guest GUEST =
            new Guest("Ada Lovelace", "ada@example.com", "3001234567", "CC-100");
    private static final LocalDate NOV_1 = LocalDate.of(2026, 11, 1);
    private static final LocalDate NOV_3 = LocalDate.of(2026, 11, 3);
    private static final LocalDate NOV_5 = LocalDate.of(2026, 11, 5);

    private static final int SINGLE_101 = 1;
    private static final int DOUBLE_201 = 6;

    private HotelFacade facade;
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        RoomRepository roomRepository = new RoomRepository();
        reservationRepository = new ReservationRepository();
        RoomService roomService = new RoomService(roomRepository, reservationRepository);
        RateService rateService = new RateService(new PricingProperties(
                Map.of(
                        RoomType.SINGLE, new BigDecimal("60.00"),
                        RoomType.DOUBLE, new BigDecimal("90.00"),
                        RoomType.SUITE, new BigDecimal("180.00")),
                new BigDecimal("1.5"),
                List.of(
                        new SeasonRange(MonthDay.of(6, 1), MonthDay.of(8, 31)),
                        new SeasonRange(MonthDay.of(12, 15), MonthDay.of(1, 6)))));
        AdditionalServiceService additionalServiceService = new AdditionalServiceService(
                new ExtrasProperties(new BigDecimal("50.00"), new BigDecimal("15.00"), new BigDecimal("30.00")));
        BillingService billingService = new BillingService(rateService);
        // Same reactions as the Spring listeners, wired by hand so the facade is tested without a context.
        DigitalKeyListener keyListener = new DigitalKeyListener(new AccessService());
        InvoiceListener invoiceListener = new InvoiceListener(billingService);
        ApplicationEventPublisher publisher = event -> {
            if (event instanceof ReservationCheckedIn checkedIn) {
                keyListener.onCheckedIn(checkedIn);
            } else if (event instanceof ReservationCheckedOut checkedOut) {
                invoiceListener.onCheckedOut(checkedOut);
            }
        };
        facade = new HotelFacade(roomService, reservationRepository, additionalServiceService, publisher);
    }

    @Test
    void createReservationStartsConfirmed() {
        Reservation reservation = facade.createReservation(SINGLE_101, NOV_1, NOV_3, GUEST);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(reservation.getId()).isNotBlank();
        assertThat(reservation.nights()).isEqualTo(2);
        assertThat(facade.getReservation(reservation.getId())).isEqualTo(reservation);
    }

    @Test
    void createReservationRejectsUnknownRoom() {
        assertThatThrownBy(() -> facade.createReservation(999, NOV_1, NOV_3, GUEST))
                .isInstanceOf(RoomNotFoundException.class);
    }

    @Test
    void createReservationRejectsInvalidDates() {
        assertThatThrownBy(() -> facade.createReservation(SINGLE_101, NOV_3, NOV_1, GUEST))
                .isInstanceOf(InvalidDatesException.class);
    }

    @Test
    void createReservationRejectsAlreadyBookedRoom() {
        facade.createReservation(SINGLE_101, NOV_1, NOV_5, GUEST);

        assertThatThrownBy(() -> facade.createReservation(SINGLE_101, NOV_3, NOV_5, GUEST))
                .isInstanceOf(RoomNotAvailableException.class);
    }

    @Test
    void addServiceWhileConfirmedAddsTheExtra() {
        Reservation reservation = facade.createReservation(SINGLE_101, NOV_1, NOV_3, GUEST); // 2 nights

        facade.addService(reservation.getId(), ServiceType.BREAKFAST);

        assertThat(reservation.getServices()).hasSize(1);
        assertThat(reservation.getServices().get(0).cost()).isEqualByComparingTo("30.00"); // 2 x 15.00
    }

    @Test
    void addServiceRejectsUnknownReservation() {
        assertThatThrownBy(() -> facade.addService("missing", ServiceType.SPA))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @Test
    void checkInIssuesAValidDigitalKey() {
        Reservation reservation = facade.createReservation(SINGLE_101, NOV_1, NOV_3, GUEST);

        facade.checkIn(reservation.getId());

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CHECKED_IN);
        assertThat(reservation.getDigitalKey()).isNotBlank();
        assertThatCode(() -> UUID.fromString(reservation.getDigitalKey())).doesNotThrowAnyException();
    }

    @Test
    void checkInTwiceIsRejected() {
        Reservation reservation = facade.createReservation(SINGLE_101, NOV_1, NOV_3, GUEST);
        facade.checkIn(reservation.getId());

        assertThatThrownBy(() -> facade.checkIn(reservation.getId()))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void checkOutRequiresCheckInFirst() {
        Reservation reservation = facade.createReservation(SINGLE_101, NOV_1, NOV_3, GUEST);

        assertThatThrownBy(() -> facade.checkOut(reservation.getId()))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void checkOutProducesTheFinalInvoiceAndReleasesTheRoom() {
        Reservation reservation = facade.createReservation(DOUBLE_201, NOV_1, NOV_3, GUEST); // DOUBLE, 2 nights
        facade.addService(reservation.getId(), ServiceType.BREAKFAST); // 2 x 15.00
        facade.checkIn(reservation.getId());

        facade.checkOut(reservation.getId());

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CHECKED_OUT);
        assertThat(reservation.getInvoice()).isNotNull();
        assertThat(reservation.getInvoice().total()).isEqualByComparingTo("210.00"); // 2 x 90.00 + 30.00
        // The room was released: the same dates can be booked again after check-out.
        Reservation again = facade.createReservation(DOUBLE_201, NOV_1, NOV_3, GUEST);
        assertThat(again.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void addServiceAfterCheckOutIsRejected() {
        Reservation reservation = facade.createReservation(SINGLE_101, NOV_1, NOV_3, GUEST);
        facade.checkIn(reservation.getId());
        facade.checkOut(reservation.getId());

        assertThatThrownBy(() -> facade.addService(reservation.getId(), ServiceType.SPA))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void getReservationRejectsUnknownId() {
        assertThatThrownBy(() -> facade.getReservation("missing"))
                .isInstanceOf(ReservationNotFoundException.class);
        assertThat(reservationRepository.findAll()).isEmpty();
    }

    // ---------- ownership ----------

    @Test
    void createReservationWithAnOwnerStoresIt() {
        Reservation reservation = facade.createReservation("user-1", SINGLE_101, NOV_1, NOV_3, GUEST);

        assertThat(reservation.getOwnerId()).isEqualTo("user-1");
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void getOwnedReservationReturnsItToItsOwner() {
        Reservation reservation = facade.createReservation("user-1", SINGLE_101, NOV_1, NOV_3, GUEST);

        assertThat(facade.getOwnedReservation("user-1", reservation.getId())).isEqualTo(reservation);
    }

    @Test
    void getOwnedReservationRejectsAnotherUser() {
        Reservation reservation = facade.createReservation("user-1", SINGLE_101, NOV_1, NOV_3, GUEST);

        assertThatThrownBy(() -> facade.getOwnedReservation("user-2", reservation.getId()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getOwnedReservationRejectsReservationsWithoutAnOwner() {
        Reservation reservation = facade.createReservation(SINGLE_101, NOV_1, NOV_3, GUEST);

        assertThatThrownBy(() -> facade.getOwnedReservation("user-1", reservation.getId()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getOwnedReservationRejectsAnUnknownId() {
        assertThatThrownBy(() -> facade.getOwnedReservation("user-1", "missing"))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @Test
    void findReservationsByOwnerReturnsOnlyTheirsMostRecentFirst() {
        Reservation early = facade.createReservation("user-1", SINGLE_101, NOV_1, NOV_3, GUEST);
        Reservation late = facade.createReservation("user-1", DOUBLE_201, NOV_3, NOV_5, GUEST);
        facade.createReservation("user-2", 2, NOV_1, NOV_3, GUEST);

        assertThat(facade.findReservationsByOwner("user-1")).containsExactly(late, early);
        assertThat(facade.findReservationsByOwner("nobody")).isEmpty();
    }
}

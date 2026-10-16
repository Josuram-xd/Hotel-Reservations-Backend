package com.hotel.reservations.event;

import com.hotel.reservations.facade.HotelFacade;
import com.hotel.reservations.model.Guest;
import com.hotel.reservations.model.Reservation;
import com.hotel.reservations.model.ServiceType;
import com.hotel.reservations.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/** With the real Spring context, the facade publishes the events and the listeners react to them. */
@SpringBootTest
@RecordApplicationEvents
class ReservationEventsIntegrationTest {

    private static final Guest GUEST = new Guest("Ada Lovelace", "ada@example.com", "3001234567", "CC-100");

    @Autowired
    private HotelFacade facade;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ApplicationEvents events;

    @Autowired
    @SuppressWarnings("unused")
    private ApplicationEventPublisher publisher;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
    }

    @Test
    void checkInPublishesAnEventAndTheListenerIssuesTheKey() {
        Reservation reservation = facade.createReservation("user-1", 1, LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 3), GUEST);

        Reservation checkedIn = facade.checkIn(reservation.getId());

        assertThat(events.stream(ReservationCheckedIn.class)).hasSize(1);
        assertThat(events.stream(ReservationCheckedIn.class).findFirst().orElseThrow().reservation())
                .isSameAs(checkedIn);
        assertThat(checkedIn.getDigitalKey()).isNotBlank();
    }

    @Test
    void checkOutPublishesAnEventAndTheListenerAttachesTheInvoice() {
        Reservation reservation = facade.createReservation("user-1", 6, LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 3), GUEST);
        facade.addService(reservation.getId(), ServiceType.SPA);
        facade.checkIn(reservation.getId());

        Reservation checkedOut = facade.checkOut(reservation.getId());

        assertThat(events.stream(ReservationCheckedOut.class)).hasSize(1);
        assertThat(checkedOut.getInvoice()).isNotNull();
        assertThat(checkedOut.getInvoice().lines()).hasSize(2); // room + spa
        assertThat(checkedOut.getInvoice().total()).isEqualByComparingTo("230.00");
    }

    @Test
    void noEventIsPublishedWhenTheTransitionIsRejected() {
        Reservation reservation = facade.createReservation("user-1", 1, LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 3), GUEST);

        try {
            facade.checkOut(reservation.getId());
        } catch (RuntimeException expected) {
            // the reservation was never checked in
        }

        assertThat(events.stream(ReservationCheckedOut.class)).isEmpty();
        assertThat(reservation.getInvoice()).isNull();
    }
}

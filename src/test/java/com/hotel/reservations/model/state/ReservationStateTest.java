package com.hotel.reservations.model.state;

import com.hotel.reservations.exception.InvalidStateException;
import com.hotel.reservations.model.AdditionalService;
import com.hotel.reservations.model.Guest;
import com.hotel.reservations.model.Reservation;
import com.hotel.reservations.model.ReservationStatus;
import com.hotel.reservations.model.Room;
import com.hotel.reservations.model.RoomType;
import com.hotel.reservations.model.ServiceType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationStateTest {

    private static final AdditionalService SPA =
            new AdditionalService(ServiceType.SPA, "Spa", new BigDecimal("50.00"));

    private static Reservation newReservation() {
        return new Reservation(new Room(1, "101", RoomType.SINGLE),
                new Guest("Ada Lovelace", "ada@example.com", "3001234567", "CC-100"),
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3));
    }

    // ---------- the states on their own ----------

    @Test
    void confirmedAllowsServicesAndMovesToCheckedIn() {
        ReservationState state = new ConfirmedState();

        assertThat(state.status()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(state.blocksAvailability()).isTrue();
        assertThatCode(state::ensureCanAddServices).doesNotThrowAnyException();
        assertThat(state.checkIn()).isInstanceOf(CheckedInState.class);
    }

    @Test
    void confirmedRejectsEverythingElse() {
        ReservationState state = new ConfirmedState();

        assertThatThrownBy(state::checkOut).isInstanceOf(InvalidStateException.class)
                .hasMessage("Cannot check out: reservation is CONFIRMED");
        assertThatThrownBy(state::ensureCanAssignDigitalKey).isInstanceOf(InvalidStateException.class)
                .hasMessage("Cannot assign a digital key: reservation is CONFIRMED");
        assertThatThrownBy(state::ensureCanAttachInvoice).isInstanceOf(InvalidStateException.class)
                .hasMessage("Cannot attach an invoice: reservation is CONFIRMED");
    }

    @Test
    void checkedInAllowsServicesAndKeyAndMovesToCheckedOut() {
        ReservationState state = new CheckedInState();

        assertThat(state.status()).isEqualTo(ReservationStatus.CHECKED_IN);
        assertThat(state.blocksAvailability()).isTrue();
        assertThatCode(state::ensureCanAddServices).doesNotThrowAnyException();
        assertThatCode(state::ensureCanAssignDigitalKey).doesNotThrowAnyException();
        assertThat(state.checkOut()).isInstanceOf(CheckedOutState.class);
    }

    @Test
    void checkedInRejectsASecondCheckInAndTheInvoice() {
        ReservationState state = new CheckedInState();

        assertThatThrownBy(state::checkIn).isInstanceOf(InvalidStateException.class)
                .hasMessage("Cannot check in: reservation is CHECKED_IN");
        assertThatThrownBy(state::ensureCanAttachInvoice).isInstanceOf(InvalidStateException.class);
    }

    @Test
    void checkedOutFreesTheRoomAndOnlyAcceptsTheInvoice() {
        ReservationState state = new CheckedOutState();

        assertThat(state.status()).isEqualTo(ReservationStatus.CHECKED_OUT);
        assertThat(state.blocksAvailability()).isFalse();
        assertThatCode(state::ensureCanAttachInvoice).doesNotThrowAnyException();
        assertThatThrownBy(state::ensureCanAddServices).isInstanceOf(InvalidStateException.class)
                .hasMessage("Cannot add services: reservation is CHECKED_OUT");
        assertThatThrownBy(state::checkIn).isInstanceOf(InvalidStateException.class);
        assertThatThrownBy(state::checkOut).isInstanceOf(InvalidStateException.class);
        assertThatThrownBy(state::ensureCanAssignDigitalKey).isInstanceOf(InvalidStateException.class);
    }

    // ---------- the reservation delegating to its state ----------

    @Test
    void aReservationWalksTheWholeLifecycleThroughItsStates() {
        Reservation reservation = newReservation();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(reservation.blocksAvailability()).isTrue();

        reservation.addService(SPA);
        reservation.checkIn();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CHECKED_IN);
        reservation.assignDigitalKey("key-1");

        reservation.checkOut();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CHECKED_OUT);
        assertThat(reservation.blocksAvailability()).isFalse();
        assertThat(reservation.getServices()).containsExactly(SPA);
        assertThat(reservation.getDigitalKey()).isEqualTo("key-1");
    }

    @Test
    void aReservationKeepsItsStateWhenATransitionIsRejected() {
        Reservation reservation = newReservation();

        assertThatThrownBy(reservation::checkOut).isInstanceOf(InvalidStateException.class);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThatCode(reservation::checkIn).doesNotThrowAnyException();
    }

    @Test
    void aReservationCannotTakeServicesAfterCheckOut() {
        Reservation reservation = newReservation();
        reservation.checkIn();
        reservation.checkOut();

        assertThatThrownBy(() -> reservation.addService(SPA)).isInstanceOf(InvalidStateException.class)
                .hasMessage("Cannot add services: reservation is CHECKED_OUT");
    }

    @Test
    void theDigitalKeyAndTheInvoiceCanOnlyBeIssuedOnce() {
        Reservation reservation = newReservation();
        reservation.checkIn();
        reservation.assignDigitalKey("key-1");

        assertThatThrownBy(() -> reservation.assignDigitalKey("key-2")).isInstanceOf(InvalidStateException.class)
                .hasMessageContaining("already issued");
    }

    @Test
    void theDigitalKeyCannotBeIssuedBeforeCheckIn() {
        Reservation reservation = newReservation();

        assertThatThrownBy(() -> reservation.assignDigitalKey("key-1")).isInstanceOf(InvalidStateException.class)
                .hasMessage("Cannot assign a digital key: reservation is CONFIRMED");
    }
}

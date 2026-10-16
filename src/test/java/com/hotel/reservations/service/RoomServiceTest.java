package com.hotel.reservations.service;

import com.hotel.reservations.exception.InvalidDatesException;
import com.hotel.reservations.exception.InvalidStateException;
import com.hotel.reservations.exception.RoomNotAvailableException;
import com.hotel.reservations.model.Guest;
import com.hotel.reservations.model.Reservation;
import com.hotel.reservations.model.ReservationStatus;
import com.hotel.reservations.model.Room;
import com.hotel.reservations.model.RoomType;
import com.hotel.reservations.repository.ReservationRepository;
import com.hotel.reservations.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoomServiceTest {

    private static final LocalDate OCT_28 = LocalDate.of(2026, 10, 28);
    private static final LocalDate NOV_1 = LocalDate.of(2026, 11, 1);
    private static final LocalDate NOV_3 = LocalDate.of(2026, 11, 3);
    private static final LocalDate NOV_5 = LocalDate.of(2026, 11, 5);
    private static final LocalDate NOV_7 = LocalDate.of(2026, 11, 7);
    private static final LocalDate NOV_8 = LocalDate.of(2026, 11, 8);

    private static final Guest GUEST =
            new Guest("Ada Lovelace", "ada@example.com", "3001234567", "CC-100");

    private RoomService roomService;
    private ReservationRepository reservationRepository;
    private Room room101;
    private Room room102;

    @BeforeEach
    void setUp() {
        RoomRepository roomRepository = new RoomRepository();
        reservationRepository = new ReservationRepository();
        roomService = new RoomService(roomRepository, reservationRepository);
        room101 = roomRepository.findById(1).orElseThrow();
        room102 = roomRepository.findById(2).orElseThrow();
    }

    @Test
    void findAvailableReturnsEveryRoomWhenNoTypeIsGiven() {
        List<Room> available = roomService.findAvailable(NOV_1, NOV_5, null);

        assertThat(available).hasSize(15);
    }

    @Test
    void findAvailableFiltersByRoomType() {
        List<Room> available = roomService.findAvailable(NOV_1, NOV_5, RoomType.SUITE);

        assertThat(available).hasSize(5)
                .allMatch(room -> room.getType() == RoomType.SUITE);
    }

    @Test
    void reserveStoresAConfirmedReservation() {
        Reservation reservation = roomService.reserve(room101, NOV_1, NOV_5, GUEST);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(reservation.getRoom()).isEqualTo(room101);
        assertThat(reservation.getGuest()).isEqualTo(GUEST);
        assertThat(reservation.nights()).isEqualTo(4);
        assertThat(reservationRepository.findAll()).containsExactly(reservation);
    }

    @Test
    void reservingBlocksTheRoomForOverlappingDates() {
        roomService.reserve(room101, NOV_1, NOV_5, GUEST);

        assertThat(roomService.isAvailable(room101, NOV_1, NOV_5)).isFalse();
        assertThat(roomService.isAvailable(room101, NOV_3, NOV_7)).isFalse();
        assertThat(roomService.isAvailable(room101, OCT_28, NOV_3)).isFalse();
        assertThat(roomService.findAvailable(NOV_1, NOV_5, null))
                .noneMatch(room -> room.getId() == room101.getId());
    }

    @Test
    void backToBackStaysDoNotOverlap() {
        roomService.reserve(room101, NOV_1, NOV_5, GUEST);

        assertThat(roomService.isAvailable(room101, NOV_5, NOV_8)).isTrue();
        assertThat(roomService.isAvailable(room101, OCT_28, NOV_1)).isTrue();
    }

    @Test
    void otherRoomsStayAvailableForTheSameDates() {
        roomService.reserve(room101, NOV_1, NOV_5, GUEST);

        assertThat(roomService.isAvailable(room102, NOV_1, NOV_5)).isTrue();
    }

    @Test
    void reserveThrowsWhenTheRangeOverlapsAnExistingReservation() {
        roomService.reserve(room101, NOV_1, NOV_5, GUEST);

        assertThatThrownBy(() -> roomService.reserve(room101, NOV_3, NOV_7, GUEST))
                .isInstanceOf(RoomNotAvailableException.class);
        assertThat(reservationRepository.findAll()).hasSize(1);
    }

    @Test
    void reserveRejectsInvalidRanges() {
        assertThatThrownBy(() -> roomService.reserve(room101, NOV_5, NOV_5, GUEST))
                .isInstanceOf(InvalidDatesException.class);
        assertThatThrownBy(() -> roomService.reserve(room101, NOV_5, NOV_1, GUEST))
                .isInstanceOf(InvalidDatesException.class);
        assertThat(reservationRepository.findAll()).isEmpty();
    }

    @Test
    void findAvailableRejectsInvalidRanges() {
        assertThatThrownBy(() -> roomService.findAvailable(NOV_1, NOV_1, null))
                .isInstanceOf(InvalidDatesException.class);
        assertThatThrownBy(() -> roomService.findAvailable(NOV_1, null, null))
                .isInstanceOf(InvalidDatesException.class);
    }

    @Test
    void releaseAfterCheckInFreesTheRoom() {
        Reservation reservation = roomService.reserve(room101, NOV_1, NOV_5, GUEST);
        reservation.checkIn();

        roomService.release(reservation);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CHECKED_OUT);
        assertThat(roomService.isAvailable(room101, NOV_1, NOV_5)).isTrue();
    }

    @Test
    void releaseRequiresTheGuestToBeCheckedIn() {
        Reservation reservation = roomService.reserve(room101, NOV_1, NOV_5, GUEST);

        assertThatThrownBy(() -> roomService.release(reservation))
                .isInstanceOf(InvalidStateException.class);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }
}

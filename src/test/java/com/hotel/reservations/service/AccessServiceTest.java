package com.hotel.reservations.service;

import com.hotel.reservations.exception.InvalidStateException;
import com.hotel.reservations.model.Guest;
import com.hotel.reservations.model.Reservation;
import com.hotel.reservations.model.Room;
import com.hotel.reservations.model.RoomType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccessServiceTest {

    private static final Guest GUEST =
            new Guest("Ada Lovelace", "ada@example.com", "3001234567", "CC-100");

    private final AccessService accessService = new AccessService();

    @Test
    void issuesUniqueParsableUuidKeys() {
        String key = accessService.issueDigitalKey();

        assertThatCode(() -> UUID.fromString(key)).doesNotThrowAnyException();
        assertThat(accessService.issueDigitalKey()).isNotEqualTo(key);
    }

    @Test
    void digitalKeyIsOnlyAssignedOnceAfterCheckIn() {
        Reservation reservation = new Reservation(new Room(1, "101", RoomType.SINGLE), GUEST,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3));

        assertThatThrownBy(() -> reservation.assignDigitalKey(accessService.issueDigitalKey()))
                .isInstanceOf(InvalidStateException.class);

        reservation.checkIn();
        String key = accessService.issueDigitalKey();
        reservation.assignDigitalKey(key);

        assertThat(reservation.getDigitalKey()).isEqualTo(key);
        assertThatThrownBy(() -> reservation.assignDigitalKey(accessService.issueDigitalKey()))
                .isInstanceOf(InvalidStateException.class);
    }
}

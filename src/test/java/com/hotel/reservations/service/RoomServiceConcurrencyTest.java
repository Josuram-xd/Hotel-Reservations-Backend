package com.hotel.reservations.service;

import com.hotel.reservations.exception.RoomNotAvailableException;
import com.hotel.reservations.model.Guest;
import com.hotel.reservations.model.Room;
import com.hotel.reservations.repository.ReservationRepository;
import com.hotel.reservations.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class RoomServiceConcurrencyTest {

    private static final Guest GUEST =
            new Guest("Ada Lovelace", "ada@example.com", "3001234567", "CC-100");
    private static final LocalDate NOV_1 = LocalDate.of(2026, 11, 1);
    private static final LocalDate NOV_3 = LocalDate.of(2026, 11, 3);

    private RoomService roomService;
    private ReservationRepository reservationRepository;
    private RoomRepository roomRepository;

    @BeforeEach
    void setUp() {
        roomRepository = new RoomRepository();
        reservationRepository = new ReservationRepository();
        roomService = new RoomService(roomRepository, reservationRepository);
    }

    @Test
    void simultaneousBookingsOfTheSameRoomAndDatesProduceExactlyOneReservation() throws Exception {
        int threads = 20;
        Room room101 = roomRepository.findById(1).orElseThrow();
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            CountDownLatch ready = new CountDownLatch(threads);
            CountDownLatch start = new CountDownLatch(1);
            List<Future<Boolean>> attempts = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                attempts.add(pool.submit(() -> {
                    ready.countDown();
                    start.await();
                    try {
                        roomService.reserve(room101, NOV_1, NOV_3, GUEST);
                        return true;
                    } catch (RoomNotAvailableException alreadyBooked) {
                        return false;
                    }
                }));
            }
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown(); // release all threads at once to maximize contention

            long successes = 0;
            for (Future<Boolean> attempt : attempts) {
                if (attempt.get(5, TimeUnit.SECONDS)) {
                    successes++;
                }
            }

            assertThat(successes).isEqualTo(1);
            assertThat(reservationRepository.findByRoom(room101.getId())).hasSize(1);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void simultaneousBookingsOfDifferentRoomsAllSucceed() throws Exception {
        List<Room> rooms = roomRepository.findAll();
        ExecutorService pool = Executors.newFixedThreadPool(rooms.size());
        try {
            List<Future<Boolean>> attempts = new ArrayList<>();
            for (Room room : rooms) {
                attempts.add(pool.submit(() -> {
                    roomService.reserve(room, NOV_1, NOV_3, GUEST);
                    return true;
                }));
            }

            long successes = 0;
            for (Future<Boolean> attempt : attempts) {
                if (attempt.get(5, TimeUnit.SECONDS)) {
                    successes++;
                }
            }

            assertThat(successes).isEqualTo(rooms.size());
            assertThat(reservationRepository.findAll()).hasSize(rooms.size());
        } finally {
            pool.shutdownNow();
        }
    }
}

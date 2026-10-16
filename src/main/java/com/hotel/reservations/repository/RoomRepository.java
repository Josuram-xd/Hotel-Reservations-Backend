package com.hotel.reservations.repository;

import com.hotel.reservations.model.Room;
import com.hotel.reservations.model.RoomType;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory store of the hotel rooms.
 * Rooms are predefined and never change at runtime, so the repository seeds
 * them once at construction and only serves lookups afterwards.
 */
@Repository
public class RoomRepository {

    private final Map<Integer, Room> roomsById;

    public RoomRepository() {
        List<Room> rooms = List.of(
                new Room(1, "101", RoomType.SINGLE),
                new Room(2, "102", RoomType.SINGLE),
                new Room(3, "103", RoomType.SINGLE),
                new Room(4, "104", RoomType.SINGLE),
                new Room(5, "105", RoomType.SINGLE),
                new Room(6, "201", RoomType.DOUBLE),
                new Room(7, "202", RoomType.DOUBLE),
                new Room(8, "203", RoomType.DOUBLE),
                new Room(9, "204", RoomType.DOUBLE),
                new Room(10, "205", RoomType.DOUBLE),
                new Room(11, "301", RoomType.SUITE),
                new Room(12, "302", RoomType.SUITE),
                new Room(13, "303", RoomType.SUITE),
                new Room(14, "304", RoomType.SUITE),
                new Room(15, "305", RoomType.SUITE)
        );
        Map<Integer, Room> map = new LinkedHashMap<>();
        for (Room room : rooms) {
            map.put(room.getId(), room);
        }
        this.roomsById = Collections.unmodifiableMap(map);
    }

    public List<Room> findAll() {
        return List.copyOf(roomsById.values());
    }

    public List<Room> findByType(RoomType type) {
        return roomsById.values().stream()
                .filter(room -> room.getType() == type)
                .toList();
    }

    public Optional<Room> findById(int id) {
        return Optional.ofNullable(roomsById.get(id));
    }
}

package com.hotel.reservations.model;

import java.util.Objects;

/** A physical room of the hotel, uniquely identified by its numeric id. */
public class Room {

    private final int id;
    private final String number;
    private final RoomType type;

    public Room(int id, String number, RoomType type) {
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("number must not be blank");
        }
        this.id = id;
        this.number = number;
        this.type = Objects.requireNonNull(type, "type must not be null");
    }

    public int getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public RoomType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Room other)) {
            return false;
        }
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "Room{id=" + id + ", number='" + number + "', type=" + type + "}";
    }
}

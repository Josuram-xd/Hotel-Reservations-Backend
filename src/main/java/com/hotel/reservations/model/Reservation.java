package com.hotel.reservations.model;

import com.hotel.reservations.exception.InvalidDatesException;
import com.hotel.reservations.exception.InvalidStateException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * A booking of one room for the date range [checkIn, checkOut).
 * The reservation owns its lifecycle: services can only be added while it is
 * CONFIRMED or CHECKED_IN, and check-in / check-out are the only legal transitions.
 */
public class Reservation {

    private final String id;
    private final Room room;
    private final Guest guest;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final List<AdditionalService> services = new ArrayList<>();

    private ReservationStatus status;
    private String digitalKey;

    public Reservation(Room room, Guest guest, LocalDate checkIn, LocalDate checkOut) {
        this.room = Objects.requireNonNull(room, "room must not be null");
        this.guest = Objects.requireNonNull(guest, "guest must not be null");
        this.checkIn = Objects.requireNonNull(checkIn, "checkIn must not be null");
        this.checkOut = Objects.requireNonNull(checkOut, "checkOut must not be null");
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidDatesException("checkOut must be after checkIn");
        }
        this.id = UUID.randomUUID().toString();
        this.status = ReservationStatus.CONFIRMED;
    }

    /** Number of nights of the stay (the range [checkIn, checkOut) is night by night). */
    public int nights() {
        return (int) ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    /** True when [from, to) overlaps the reservation range [checkIn, checkOut). */
    public boolean overlaps(LocalDate from, LocalDate to) {
        return checkIn.isBefore(to) && from.isBefore(checkOut);
    }

    /** True while this reservation still blocks the availability of its room. */
    public boolean blocksAvailability() {
        return status != ReservationStatus.CHECKED_OUT;
    }

    /** Adds an extra service. Only allowed while CONFIRMED or CHECKED_IN. */
    public void addService(AdditionalService service) {
        requireStatus("add services", ReservationStatus.CONFIRMED, ReservationStatus.CHECKED_IN);
        services.add(Objects.requireNonNull(service, "service must not be null"));
    }

    /** CONFIRMED -> CHECKED_IN. */
    public void checkIn() {
        requireStatus("check in", ReservationStatus.CONFIRMED);
        status = ReservationStatus.CHECKED_IN;
    }

    /** CHECKED_IN -> CHECKED_OUT. Releases the room. */
    public void checkOut() {
        requireStatus("check out", ReservationStatus.CHECKED_IN);
        status = ReservationStatus.CHECKED_OUT;
    }

    /** Stores the digital key issued at check-in. Only allowed once, after check-in. */
    public void assignDigitalKey(String key) {
        requireStatus("assign a digital key", ReservationStatus.CHECKED_IN);
        if (digitalKey != null) {
            throw new InvalidStateException("Cannot assign a digital key: it was already issued for this reservation");
        }
        this.digitalKey = Objects.requireNonNull(key, "key must not be null");
    }

    private void requireStatus(String action, ReservationStatus... allowed) {
        for (ReservationStatus candidate : allowed) {
            if (status == candidate) {
                return;
            }
        }
        throw new InvalidStateException("Cannot " + action + ": reservation is " + status);
    }

    public String getId() {
        return id;
    }

    public Room getRoom() {
        return room;
    }

    public Guest getGuest() {
        return guest;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public List<AdditionalService> getServices() {
        return Collections.unmodifiableList(services);
    }

    public String getDigitalKey() {
        return digitalKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Reservation other)) {
            return false;
        }
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Reservation{id='" + id + "', room=" + room.getNumber()
                + ", checkIn=" + checkIn + ", checkOut=" + checkOut + ", status=" + status + "}";
    }
}

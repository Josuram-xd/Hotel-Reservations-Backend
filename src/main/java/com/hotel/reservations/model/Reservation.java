package com.hotel.reservations.model;

import com.hotel.reservations.exception.InvalidDatesException;
import com.hotel.reservations.exception.InvalidStateException;
import com.hotel.reservations.model.state.ConfirmedState;
import com.hotel.reservations.model.state.ReservationState;

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
    private final String ownerId;

    private ReservationState state = new ConfirmedState();
    private String digitalKey;
    private Invoice invoice;

    /** A reservation with no owner (not linked to any user account). */
    public Reservation(Room room, Guest guest, LocalDate checkIn, LocalDate checkOut) {
        this(room, guest, checkIn, checkOut, null);
    }

    /** A reservation owned by the user with the given id; only that user may manage it. */
    public Reservation(Room room, Guest guest, LocalDate checkIn, LocalDate checkOut, String ownerId) {
        this.room = Objects.requireNonNull(room, "room must not be null");
        this.guest = Objects.requireNonNull(guest, "guest must not be null");
        this.checkIn = Objects.requireNonNull(checkIn, "checkIn must not be null");
        this.checkOut = Objects.requireNonNull(checkOut, "checkOut must not be null");
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidDatesException("checkOut must be after checkIn");
        }
        this.ownerId = ownerId;
        this.id = UUID.randomUUID().toString();
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
        return state.blocksAvailability();
    }

    /** Adds an extra service. Only allowed while CONFIRMED or CHECKED_IN. */
    public void addService(AdditionalService service) {
        state.ensureCanAddServices();
        services.add(Objects.requireNonNull(service, "service must not be null"));
    }

    /** CONFIRMED -> CHECKED_IN. */
    public void checkIn() {
        state = state.checkIn();
    }

    /** CHECKED_IN -> CHECKED_OUT. Releases the room. */
    public void checkOut() {
        state = state.checkOut();
    }

    /** Stores the digital key issued at check-in. Only allowed once, after check-in. */
    public void assignDigitalKey(String key) {
        state.ensureCanAssignDigitalKey();
        if (digitalKey != null) {
            throw new InvalidStateException("Cannot assign a digital key: it was already issued for this reservation");
        }
        this.digitalKey = Objects.requireNonNull(key, "key must not be null");
    }

    /** Stores the final invoice. Only allowed once, after check-out. */
    public void attachInvoice(Invoice invoice) {
        state.ensureCanAttachInvoice();
        if (this.invoice != null) {
            throw new InvalidStateException("Cannot attach an invoice: it was already issued for this reservation");
        }
        this.invoice = Objects.requireNonNull(invoice, "invoice must not be null");
    }

    public String getId() {
        return id;
    }

    /** Id of the user that owns the reservation, or null when it has no owner. */
    public String getOwnerId() {
        return ownerId;
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
        return state.status();
    }

    public List<AdditionalService> getServices() {
        return Collections.unmodifiableList(services);
    }

    public String getDigitalKey() {
        return digitalKey;
    }

    public Invoice getInvoice() {
        return invoice;
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
                + ", checkIn=" + checkIn + ", checkOut=" + checkOut + ", status=" + state.status() + "}";
    }
}

package com.hotel.reservations.model.state;

import com.hotel.reservations.exception.InvalidStateException;
import com.hotel.reservations.model.ReservationStatus;

/**
 * State pattern: each lifecycle state of a reservation decides which actions
 * are legal and which state comes next. By default every action is rejected;
 * a concrete state only overrides what it allows, so an illegal transition can
 * never succeed by accident.
 *
 * <pre>
 *   CONFIRMED --checkIn--> CHECKED_IN --checkOut--> CHECKED_OUT
 * </pre>
 */
public interface ReservationState {

    /** The public status that corresponds to this state. */
    ReservationStatus status();

    /** True while a reservation in this state still blocks the availability of its room. */
    default boolean blocksAvailability() {
        return true;
    }

    /** Fails unless extra services can be added in this state. */
    default void ensureCanAddServices() {
        throw reject("add services");
    }

    /** The state after a check-in, or an InvalidStateException when check-in is not allowed. */
    default ReservationState checkIn() {
        throw reject("check in");
    }

    /** The state after a check-out, or an InvalidStateException when check-out is not allowed. */
    default ReservationState checkOut() {
        throw reject("check out");
    }

    /** Fails unless the digital key can be issued in this state. */
    default void ensureCanAssignDigitalKey() {
        throw reject("assign a digital key");
    }

    /** Fails unless the final invoice can be attached in this state. */
    default void ensureCanAttachInvoice() {
        throw reject("attach an invoice");
    }

    /** The exception for an action this state does not allow. */
    default InvalidStateException reject(String action) {
        return new InvalidStateException("Cannot " + action + ": reservation is " + status());
    }
}

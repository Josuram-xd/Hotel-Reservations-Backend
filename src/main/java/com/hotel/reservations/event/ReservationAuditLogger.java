package com.hotel.reservations.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Observer: logs every stay milestone. It shows the point of the pattern: a new
 * reaction to check-in or check-out is a new listener, and the facade does not change.
 */
@Component
public class ReservationAuditLogger {

    private static final Logger LOG = LoggerFactory.getLogger(ReservationAuditLogger.class);

    @EventListener
    public void onCheckedIn(ReservationCheckedIn event) {
        LOG.info("Reservation {} checked in (room {})",
                event.reservation().getId(), event.reservation().getRoom().getNumber());
    }

    @EventListener
    public void onCheckedOut(ReservationCheckedOut event) {
        LOG.info("Reservation {} checked out (room {} released)",
                event.reservation().getId(), event.reservation().getRoom().getNumber());
    }
}

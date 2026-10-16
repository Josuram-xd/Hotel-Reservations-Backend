package com.hotel.reservations.event;

import com.hotel.reservations.service.AccessService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Observer: issues the digital key of the guest as soon as the check-in happens. */
@Component
public class DigitalKeyListener {

    private final AccessService accessService;

    public DigitalKeyListener(AccessService accessService) {
        this.accessService = accessService;
    }

    @EventListener
    public void onCheckedIn(ReservationCheckedIn event) {
        event.reservation().assignDigitalKey(accessService.issueDigitalKey());
    }
}

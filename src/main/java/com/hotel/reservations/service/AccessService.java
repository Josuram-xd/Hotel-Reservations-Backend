package com.hotel.reservations.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

/** Issues the digital keys that guests use to open their room after check-in. */
@Service
public class AccessService {

    /** Issues a new digital key (opaque UUID). */
    public String issueDigitalKey() {
        return UUID.randomUUID().toString();
    }
}

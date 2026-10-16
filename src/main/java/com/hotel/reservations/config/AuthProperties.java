package com.hotel.reservations.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** Session settings, configured in application.yml under {@code hotel.auth}. */
@ConfigurationProperties(prefix = "hotel.auth")
public record AuthProperties(Duration tokenTtl) {

    private static final Duration DEFAULT_TOKEN_TTL = Duration.ofHours(8);

    public AuthProperties {
        if (tokenTtl == null || tokenTtl.isZero() || tokenTtl.isNegative()) {
            tokenTtl = DEFAULT_TOKEN_TTL;
        }
    }
}

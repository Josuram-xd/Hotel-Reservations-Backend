package com.hotel.reservations.web;

/** Reads the token out of an {@code Authorization: Bearer <token>} header. */
final class BearerTokens {

    private static final String PREFIX = "Bearer ";

    private BearerTokens() {
    }

    /** The token in the header value, or null when the header is missing or is not a Bearer header. */
    static String from(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.regionMatches(true, 0, PREFIX, 0, PREFIX.length())) {
            return null;
        }
        String token = authorizationHeader.substring(PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }
}

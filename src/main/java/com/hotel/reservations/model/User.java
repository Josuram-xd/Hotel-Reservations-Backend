package com.hotel.reservations.model;

import java.util.Objects;
import java.util.UUID;

/**
 * A registered user. The password is never stored in clear text: only its hash.
 * The email is the login identifier and is always kept lower-cased and trimmed.
 */
public record User(String id, String fullName, String email, String passwordHash) {

    public User {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(fullName, "fullName must not be null");
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(passwordHash, "passwordHash must not be null");
    }

    /** Creates a new user with a generated id and a normalized email. */
    public static User create(String fullName, String email, String passwordHash) {
        return new User(UUID.randomUUID().toString(), fullName.trim(), normalizeEmail(email), passwordHash);
    }

    /** The canonical form of an email: trimmed and lower-cased. */
    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}

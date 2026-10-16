package com.hotel.reservations.repository;

import com.hotel.reservations.model.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store of the registered users, keyed by their normalized email.
 * The email is unique: {@link #saveIfEmailFree} checks and stores atomically,
 * so two simultaneous registrations cannot create the same account.
 */
@Repository
public class UserRepository {

    private final ConcurrentHashMap<String, User> usersByEmail = new ConcurrentHashMap<>();

    /** Stores the user unless the email is already registered. Returns whether it was stored. */
    public boolean saveIfEmailFree(User user) {
        return usersByEmail.putIfAbsent(User.normalizeEmail(user.email()), user) == null;
    }

    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(usersByEmail.get(User.normalizeEmail(email)));
    }

    public Optional<User> findById(String id) {
        return usersByEmail.values().stream()
                .filter(user -> user.id().equals(id))
                .findFirst();
    }

    /** Removes every stored user. Used by tests to isolate scenarios. */
    public void deleteAll() {
        usersByEmail.clear();
    }
}

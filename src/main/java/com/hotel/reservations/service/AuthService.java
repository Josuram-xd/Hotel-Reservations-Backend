package com.hotel.reservations.service;

import com.hotel.reservations.config.AuthProperties;
import com.hotel.reservations.exception.EmailAlreadyRegisteredException;
import com.hotel.reservations.exception.InvalidCredentialsException;
import com.hotel.reservations.exception.UnauthorizedException;
import com.hotel.reservations.model.User;
import com.hotel.reservations.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registers and authenticates users. Passwords are stored as BCrypt hashes.
 * A successful register or login opens a session identified by an opaque random
 * token that expires after {@code hotel.auth.token-ttl}; sessions live in memory.
 */
@Service
public class AuthService {

    /** An opened session: the token the client must send and the user it belongs to. */
    public record AuthSession(String token, User user) {
    }

    private record Session(String userId, Instant expiresAt) {
    }

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthProperties properties;
    private final Clock clock;
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
    /** Hash compared against when the email is unknown, so both login failures take the same time. */
    private final String unknownUserHash;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthProperties properties, Clock clock) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
        this.clock = clock;
        this.unknownUserHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    /** Creates the account and opens a session. Fails when the email is already registered. */
    public AuthSession register(String fullName, String email, String rawPassword) {
        User user = User.create(fullName, email, passwordEncoder.encode(rawPassword));
        if (!userRepository.saveIfEmailFree(user)) {
            throw new EmailAlreadyRegisteredException("The email " + user.email() + " is already registered");
        }
        return openSession(user);
    }

    /** Opens a session when the email and password match. The failure does not reveal which one was wrong. */
    public AuthSession login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email).orElse(null);
        String hash = user == null ? unknownUserHash : user.passwordHash();
        boolean matches = passwordEncoder.matches(rawPassword, hash);
        if (user == null || !matches) {
            throw new InvalidCredentialsException("The email or the password is incorrect");
        }
        return openSession(user);
    }

    /** The user that owns a valid, non-expired token. Expired tokens are discarded. */
    public User authenticate(String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Authentication is required");
        }
        Session session = sessions.get(token);
        if (session == null) {
            throw new UnauthorizedException("The session is not valid");
        }
        if (!clock.instant().isBefore(session.expiresAt())) {
            sessions.remove(token);
            throw new UnauthorizedException("The session has expired");
        }
        return userRepository.findById(session.userId())
                .orElseThrow(() -> new UnauthorizedException("The session is not valid"));
    }

    /** Closes the session of the token. Closing an unknown token is not an error. */
    public void logout(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    private AuthSession openSession(User user) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, new Session(user.id(), clock.instant().plus(properties.tokenTtl())));
        return new AuthSession(token, user);
    }
}

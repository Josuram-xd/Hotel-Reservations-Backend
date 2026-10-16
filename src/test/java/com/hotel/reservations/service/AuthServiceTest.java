package com.hotel.reservations.service;

import com.hotel.reservations.config.AuthProperties;
import com.hotel.reservations.exception.EmailAlreadyRegisteredException;
import com.hotel.reservations.exception.InvalidCredentialsException;
import com.hotel.reservations.exception.UnauthorizedException;
import com.hotel.reservations.repository.UserRepository;
import com.hotel.reservations.service.AuthService.AuthSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthServiceTest {

    /** A clock the test can move forward to expire tokens without waiting. */
    private static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-11-01T10:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    private MutableClock clock;
    private UserRepository userRepository;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        clock = new MutableClock();
        userRepository = new UserRepository();
        authService = new AuthService(userRepository, new BCryptPasswordEncoder(4),
                new AuthProperties(Duration.ofHours(1)), clock);
    }

    @Test
    void registerStoresAHashedPasswordAndOpensASession() {
        AuthSession session = authService.register("Ada Lovelace", "Ada@Example.com", "secret-pass");

        assertThat(session.token()).isNotBlank();
        assertThat(session.user().email()).isEqualTo("ada@example.com");
        assertThat(session.user().passwordHash()).isNotEqualTo("secret-pass").startsWith("$2");
        assertThat(authService.authenticate(session.token())).isEqualTo(session.user());
    }

    @Test
    void registerRejectsAnEmailThatAlreadyHasAnAccount() {
        authService.register("Ada", "ada@example.com", "secret-pass");

        assertThatThrownBy(() -> authService.register("Other", " ADA@example.com ", "another-pass"))
                .isInstanceOf(EmailAlreadyRegisteredException.class);
    }

    @Test
    void loginWithTheRightPasswordOpensAnIndependentSession() {
        AuthSession registered = authService.register("Ada", "ada@example.com", "secret-pass");

        AuthSession loggedIn = authService.login("ADA@example.com", "secret-pass");

        assertThat(loggedIn.token()).isNotEqualTo(registered.token());
        assertThat(loggedIn.user()).isEqualTo(registered.user());
    }

    @Test
    void loginFailsTheSameWayForAWrongPasswordAndAnUnknownEmail() {
        authService.register("Ada", "ada@example.com", "secret-pass");

        assertThatThrownBy(() -> authService.login("ada@example.com", "wrong-pass"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("The email or the password is incorrect");
        assertThatThrownBy(() -> authService.login("nobody@example.com", "secret-pass"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("The email or the password is incorrect");
    }

    @Test
    void authenticateRejectsMissingAndUnknownTokens() {
        assertThatThrownBy(() -> authService.authenticate(null)).isInstanceOf(UnauthorizedException.class);
        assertThatThrownBy(() -> authService.authenticate("  ")).isInstanceOf(UnauthorizedException.class);
        assertThatThrownBy(() -> authService.authenticate("not-a-token")).isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void tokensExpireAfterTheConfiguredTtl() {
        AuthSession session = authService.register("Ada", "ada@example.com", "secret-pass");

        clock.advance(Duration.ofMinutes(59));
        assertThat(authService.authenticate(session.token())).isEqualTo(session.user());

        clock.advance(Duration.ofMinutes(1));
        assertThatThrownBy(() -> authService.authenticate(session.token()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("The session has expired");
    }

    @Test
    void logoutClosesOnlyThatSession() {
        AuthSession first = authService.register("Ada", "ada@example.com", "secret-pass");
        AuthSession second = authService.login("ada@example.com", "secret-pass");

        authService.logout(first.token());

        assertThatThrownBy(() -> authService.authenticate(first.token())).isInstanceOf(UnauthorizedException.class);
        assertThat(authService.authenticate(second.token())).isEqualTo(second.user());
        authService.logout("unknown-token");
        authService.logout(null);
    }

    @Test
    void aTokenWhoseUserNoLongerExistsIsRejected() {
        AuthSession session = authService.register("Ada", "ada@example.com", "secret-pass");
        userRepository.deleteAll();

        assertThatThrownBy(() -> authService.authenticate(session.token()))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void authPropertiesFallBackToEightHoursWhenTheTtlIsMissingOrInvalid() {
        assertThat(new AuthProperties(null).tokenTtl()).isEqualTo(Duration.ofHours(8));
        assertThat(new AuthProperties(Duration.ZERO).tokenTtl()).isEqualTo(Duration.ofHours(8));
        assertThat(new AuthProperties(Duration.ofMinutes(-5)).tokenTtl()).isEqualTo(Duration.ofHours(8));
        assertThat(new AuthProperties(Duration.ofMinutes(30)).tokenTtl()).isEqualTo(Duration.ofMinutes(30));
    }
}

package com.hotel.reservations.web;

import com.hotel.reservations.dto.ApiErrorResponse;
import com.hotel.reservations.exception.EmailAlreadyRegisteredException;
import com.hotel.reservations.exception.ForbiddenException;
import com.hotel.reservations.exception.InvalidCredentialsException;
import com.hotel.reservations.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** The authentication errors are translated into the codes of the API contract. */
class GlobalExceptionHandlerAuthTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void unauthorizedMapsTo401() {
        ApiErrorResponse response = handler.handleUnauthorized(new UnauthorizedException("no session"));

        assertThat(response.status()).isEqualTo(401);
        assertThat(response.code()).isEqualTo("UNAUTHORIZED");
        assertThat(response.message()).isEqualTo("no session");
    }

    @Test
    void invalidCredentialsMapsTo401() {
        ApiErrorResponse response = handler.handleInvalidCredentials(new InvalidCredentialsException("bad"));

        assertThat(response.status()).isEqualTo(401);
        assertThat(response.code()).isEqualTo("INVALID_CREDENTIALS");
    }

    @Test
    void forbiddenMapsTo403() {
        ApiErrorResponse response = handler.handleForbidden(new ForbiddenException("not yours"));

        assertThat(response.status()).isEqualTo(403);
        assertThat(response.code()).isEqualTo("FORBIDDEN");
    }

    @Test
    void duplicateEmailMapsTo409() {
        ApiErrorResponse response = handler.handleEmailAlreadyRegistered(new EmailAlreadyRegisteredException("dup"));

        assertThat(response.status()).isEqualTo(409);
        assertThat(response.code()).isEqualTo("EMAIL_ALREADY_REGISTERED");
    }
}

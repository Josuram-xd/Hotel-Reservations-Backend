package com.hotel.reservations.web;

import com.hotel.reservations.dto.AuthResponse;
import com.hotel.reservations.dto.LoginRequest;
import com.hotel.reservations.dto.RegisterRequest;
import com.hotel.reservations.dto.UserResponse;
import com.hotel.reservations.service.AuthService;
import com.hotel.reservations.service.AuthService.AuthSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Account endpoints: register, login, current user and logout. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Creates an account and opens a session. */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return toResponse(authService.register(request.fullName(), request.email(), request.password()));
    }

    /** Opens a session with the email and password. */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return toResponse(authService.login(request.email(), request.password()));
    }

    /** The user of the token sent in the Authorization header. */
    @GetMapping("/me")
    public UserResponse me(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return UserResponse.from(authService.authenticate(BearerTokens.from(authorization)));
    }

    /** Closes the session of the token. Idempotent: an unknown or missing token also returns 204. */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        authService.logout(BearerTokens.from(authorization));
    }

    private static AuthResponse toResponse(AuthSession session) {
        return new AuthResponse(session.token(), UserResponse.from(session.user()));
    }
}

package com.hotel.reservations.web;

import com.hotel.reservations.model.User;
import com.hotel.reservations.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Requires a valid bearer token on the protected endpoints and exposes the
 * authenticated user to the controllers through a request attribute.
 * A missing, unknown or expired token raises UnauthorizedException, which the
 * global exception handler turns into a 401 with the code UNAUTHORIZED.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    /** Request attribute holding the authenticated {@link User}. */
    public static final String USER_ATTRIBUTE = "authenticatedUser";

    private final AuthService authService;

    public AuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true; // CORS preflight carries no credentials
        }
        String token = BearerTokens.from(request.getHeader(HttpHeaders.AUTHORIZATION));
        User user = authService.authenticate(token);
        request.setAttribute(USER_ATTRIBUTE, user);
        return true;
    }
}

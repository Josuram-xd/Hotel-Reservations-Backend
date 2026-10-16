package com.hotel.reservations.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;

/** Infrastructure beans used by the authentication service. */
@Configuration
public class SecurityBeansConfig {

    /** BCrypt hashing for passwords (only spring-security-crypto, not the full Spring Security). */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Injectable clock so token expiry can be tested without waiting. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}

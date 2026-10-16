package com.hotel.reservations.config;

import com.hotel.reservations.web.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Decides which endpoints need a session. Everything under {@code /api/hotel}
 * is protected except the availability search, which stays public so guests
 * can browse rooms and prices before creating an account.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/hotel/**")
                .excludePathPatterns("/api/hotel/disponibilidad");
    }
}

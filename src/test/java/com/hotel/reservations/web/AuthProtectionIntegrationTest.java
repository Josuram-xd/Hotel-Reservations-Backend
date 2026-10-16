package com.hotel.reservations.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Which endpoints need a session and which stay public. */
@SpringBootTest
@AutoConfigureMockMvc
class AuthProtectionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static List<MockHttpServletRequestBuilder> protectedRequests() {
        return List.of(
                post("/api/hotel/reservar").contentType(MediaType.APPLICATION_JSON).content("{}"),
                get("/api/hotel/reserva/any"),
                post("/api/hotel/servicios/any").contentType(MediaType.APPLICATION_JSON).content("{}"),
                put("/api/hotel/checkin/any"),
                put("/api/hotel/checkout/any"));
    }

    @Test
    void protectedEndpointsRejectRequestsWithoutAToken() throws Exception {
        for (MockHttpServletRequestBuilder request : protectedRequests()) {
            mockMvc.perform(request)
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        }
    }

    @Test
    void protectedEndpointsRejectAnInvalidToken() throws Exception {
        mockMvc.perform(get("/api/hotel/reserva/any").header("Authorization", "Bearer made-up-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        mockMvc.perform(get("/api/hotel/reserva/any").header("Authorization", "Token abc"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void availabilityStaysPublic() throws Exception {
        mockMvc.perform(get("/api/hotel/disponibilidad")
                        .param("checkIn", "2026-11-01")
                        .param("checkOut", "2026-11-03"))
                .andExpect(status().isOk());
    }

    @Test
    void corsPreflightIsNotBlockedByTheAuthentication() throws Exception {
        mockMvc.perform(options("/api/hotel/reservar")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }
}

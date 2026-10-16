package com.hotel.reservations.web;

import com.hotel.reservations.repository.ReservationRepository;
import com.hotel.reservations.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** A reservation can only be read and managed by the user who created it. */
@SpringBootTest
@AutoConfigureMockMvc
class ReservationOwnershipIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    private String ownerToken;
    private String intruderToken;
    private String reservationId;

    @BeforeEach
    void setUp() throws Exception {
        reservationRepository.deleteAll();
        userRepository.deleteAll();
        ownerToken = register("owner@example.com");
        intruderToken = register("intruder@example.com");
        reservationId = createReservationAsOwner();
    }

    @Test
    void anotherUserCannotReadTheReservation() throws Exception {
        mockMvc.perform(get("/api/hotel/reserva/{id}", reservationId).header("Authorization", bearer(intruderToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void anotherUserCannotAddServices() throws Exception {
        mockMvc.perform(post("/api/hotel/servicios/{id}", reservationId)
                        .header("Authorization", bearer(intruderToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"serviceType\": \"SPA\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void anotherUserCannotCheckInOrCheckOut() throws Exception {
        mockMvc.perform(put("/api/hotel/checkin/{id}", reservationId).header("Authorization", bearer(intruderToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        mockMvc.perform(put("/api/hotel/checkout/{id}", reservationId).header("Authorization", bearer(intruderToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void aRejectedActionLeavesTheReservationUntouched() throws Exception {
        mockMvc.perform(put("/api/hotel/checkin/{id}", reservationId).header("Authorization", bearer(intruderToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/hotel/reserva/{id}", reservationId).header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.digitalKey").doesNotExist());
    }

    @Test
    void theOwnerCanManageTheReservationEndToEnd() throws Exception {
        mockMvc.perform(post("/api/hotel/servicios/{id}", reservationId)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"serviceType\": \"SPA\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/hotel/checkin/{id}", reservationId).header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_IN"));
        mockMvc.perform(put("/api/hotel/checkout/{id}", reservationId).header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_OUT"));
    }

    @Test
    void anUnknownReservationIsNotFoundRatherThanForbidden() throws Exception {
        mockMvc.perform(get("/api/hotel/reserva/{id}", "missing").header("Authorization", bearer(intruderToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESERVATION_NOT_FOUND"));
    }

    @Test
    void aTokenClosedByLogoutCannotManageReservationsAnymore() throws Exception {
        mockMvc.perform(post("/api/auth/logout").header("Authorization", bearer(ownerToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/hotel/reserva/{id}", reservationId).header("Authorization", bearer(ownerToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    private String register(String email) throws Exception {
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\": \"Test User\", \"email\": \"" + email
                                + "\", \"password\": \"secret-pass\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.token");
    }

    private String createReservationAsOwner() throws Exception {
        String response = mockMvc.perform(post("/api/hotel/reservar")
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"roomId": 1, "checkIn": "2026-11-01", "checkOut": "2026-11-03",
                                 "guest": {"fullName": "Ada Lovelace", "email": "ada@example.com",
                                           "phone": "3001234567", "document": "CC-100"}}
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.id");
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}

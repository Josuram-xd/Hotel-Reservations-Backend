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

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** End-to-end tests of every endpoint, asserting the frontend API contract. */
@SpringBootTest
@AutoConfigureMockMvc
class HotelApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        reservationRepository.deleteAll();
        userRepository.deleteAll();
        token = registerAndGetToken("ada@example.com");
    }

    // ---------- GET /api/hotel/disponibilidad ----------

    @Test
    void availabilityReturnsEveryRoomWithItsPricePreview() throws Exception {
        mockMvc.perform(get("/api/hotel/disponibilidad")
                        .param("checkIn", "2026-11-01")
                        .param("checkOut", "2026-11-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(15)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].number").value("101"))
                .andExpect(jsonPath("$[0].type").value("SINGLE"))
                .andExpect(jsonPath("$[0].pricePerNight").value(60.0))
                .andExpect(jsonPath("$[0].nights").value(2))
                .andExpect(jsonPath("$[0].highSeasonNights").value(0))
                .andExpect(jsonPath("$[0].totalPrice").value(120.0));
    }

    @Test
    void availabilityFiltersByTypeAndAppliesTheHighSeason() throws Exception {
        mockMvc.perform(get("/api/hotel/disponibilidad")
                        .param("checkIn", "2026-07-01")
                        .param("checkOut", "2026-07-03")
                        .param("type", "DOUBLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[*].type", everyItem(is("DOUBLE"))))
                .andExpect(jsonPath("$[0].pricePerNight").value(90.0))
                .andExpect(jsonPath("$[0].nights").value(2))
                .andExpect(jsonPath("$[0].highSeasonNights").value(2))
                .andExpect(jsonPath("$[0].totalPrice").value(270.0)); // 2 x 90.00 x 1.5
    }

    @Test
    void availabilityRejectsAnInvalidDateRange() throws Exception {
        mockMvc.perform(get("/api/hotel/disponibilidad")
                        .param("checkIn", "2026-11-03")
                        .param("checkOut", "2026-11-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_DATES"));
    }

    @Test
    void availabilityRejectsAnUnknownRoomType() throws Exception {
        mockMvc.perform(get("/api/hotel/disponibilidad")
                        .param("checkIn", "2026-11-01")
                        .param("checkOut", "2026-11-03")
                        .param("type", "TRIPLE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    // ---------- POST /api/hotel/reservar ----------

    @Test
    void createReservationReturns201WithTheContractPayload() throws Exception {
        mockMvc.perform(post("/api/hotel/reservar").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationBody(1, "2026-11-01", "2026-11-03")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.room.id").value(1))
                .andExpect(jsonPath("$.room.number").value("101"))
                .andExpect(jsonPath("$.room.type").value("SINGLE"))
                .andExpect(jsonPath("$.guest.fullName").value("Ada Lovelace"))
                .andExpect(jsonPath("$.checkIn").value("2026-11-01"))
                .andExpect(jsonPath("$.checkOut").value("2026-11-03"))
                .andExpect(jsonPath("$.nights").value(2))
                .andExpect(jsonPath("$.roomTotal").value(120.0))
                .andExpect(jsonPath("$.services", hasSize(0)))
                .andExpect(jsonPath("$.servicesTotal").value(0.0))
                .andExpect(jsonPath("$.total").value(120.0))
                .andExpect(jsonPath("$.digitalKey").value(nullValue()))
                .andExpect(jsonPath("$.invoice").value(nullValue()));
    }

    @Test
    void createReservationValidatesTheGuestFields() throws Exception {
        String body = """
                {"roomId": 1, "checkIn": "2026-11-01", "checkOut": "2026-11-03",
                 "guest": {"fullName": "", "email": "not-an-email", "phone": "", "document": ""}}
                """;
        mockMvc.perform(post("/api/hotel/reservar").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[*].field",
                        hasItems("guest.fullName", "guest.email", "guest.phone", "guest.document")));
    }

    @Test
    void createReservationRejectsAnUnknownRoom() throws Exception {
        mockMvc.perform(post("/api/hotel/reservar").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationBody(999, "2026-11-01", "2026-11-03")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ROOM_NOT_FOUND"));
    }

    @Test
    void createReservationRejectsAnAlreadyBookedRoom() throws Exception {
        createReservation(6, "2026-11-01", "2026-11-03");

        mockMvc.perform(post("/api/hotel/reservar").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationBody(6, "2026-11-02", "2026-11-04")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ROOM_NOT_AVAILABLE"));
    }

    // ---------- POST /api/hotel/servicios/{reservaId} ----------

    @Test
    void addServiceAddsTheExtraWithItsConfiguredPrice() throws Exception {
        String id = createReservation(1, "2026-11-01", "2026-11-03"); // 2 nights

        mockMvc.perform(post("/api/hotel/servicios/{reservaId}", id).header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"serviceType\": \"BREAKFAST\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.services", hasSize(1)))
                .andExpect(jsonPath("$.services[0].type").value("BREAKFAST"))
                .andExpect(jsonPath("$.services[0].name").value("Desayuno"))
                .andExpect(jsonPath("$.services[0].cost").value(30.0)) // 2 x 15.00
                .andExpect(jsonPath("$.servicesTotal").value(30.0))
                .andExpect(jsonPath("$.total").value(150.0)); // 120.00 + 30.00
    }

    @Test
    void addServiceRejectsAnUnknownReservation() throws Exception {
        mockMvc.perform(post("/api/hotel/servicios/{reservaId}", "missing").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"serviceType\": \"SPA\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESERVATION_NOT_FOUND"));
    }

    @Test
    void addServiceRejectsAnInvalidServiceType() throws Exception {
        String id = createReservation(1, "2026-11-01", "2026-11-03");

        mockMvc.perform(post("/api/hotel/servicios/{reservaId}", id).header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"serviceType\": \"SAUNA\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    // ---------- PUT /api/hotel/checkin/{reservaId} ----------

    @Test
    void checkInIssuesTheDigitalKey() throws Exception {
        String id = createReservation(1, "2026-11-01", "2026-11-03");

        mockMvc.perform(put("/api/hotel/checkin/{reservaId}", id).header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_IN"))
                .andExpect(jsonPath("$.digitalKey").isNotEmpty());
    }

    // ---------- PUT /api/hotel/checkout/{reservaId} ----------

    @Test
    void checkOutIssuesTheInvoiceAndReleasesTheRoom() throws Exception {
        String id = createReservation(6, "2026-11-01", "2026-11-03"); // DOUBLE, 2 nights
        mockMvc.perform(post("/api/hotel/servicios/{reservaId}", id).header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"serviceType\": \"SPA\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/hotel/checkin/{reservaId}", id).header("Authorization", bearer())).andExpect(status().isOk());

        mockMvc.perform(put("/api/hotel/checkout/{reservaId}", id).header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_OUT"))
                .andExpect(jsonPath("$.invoice.number").value(startsWith("FAC-")))
                .andExpect(jsonPath("$.invoice.lines", hasSize(2))) // room + spa
                .andExpect(jsonPath("$.invoice.subtotal").value(230.0)) // 180.00 + 50.00
                .andExpect(jsonPath("$.invoice.total").value(230.0));

        // The room was released: the same dates can be booked again.
        mockMvc.perform(post("/api/hotel/reservar").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationBody(6, "2026-11-01", "2026-11-03")))
                .andExpect(status().isCreated());
    }

    @Test
    void checkOutWithoutCheckInIsRejected() throws Exception {
        String id = createReservation(1, "2026-11-01", "2026-11-03");

        mockMvc.perform(put("/api/hotel/checkout/{reservaId}", id).header("Authorization", bearer()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATE"));
    }

    // ---------- GET /api/hotel/reserva/{reservaId} ----------

    @Test
    void getReservationReturnsTheFullPayload() throws Exception {
        String id = createReservation(1, "2026-11-01", "2026-11-03");
        mockMvc.perform(post("/api/hotel/servicios/{reservaId}", id).header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"serviceType\": \"TRANSFER\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/hotel/reserva/{reservaId}", id).header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.services", hasSize(1)))
                .andExpect(jsonPath("$.services[0].name").value("Traslado"))
                .andExpect(jsonPath("$.total").value(150.0)); // 120.00 + 30.00
    }

    @Test
    void getReservationRejectsAnUnknownId() throws Exception {
        mockMvc.perform(get("/api/hotel/reserva/{reservaId}", "missing").header("Authorization", bearer()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESERVATION_NOT_FOUND"));
    }

    // ---------- Full lifecycle ----------

    @Test
    void fullLifecycleThroughTheApi() throws Exception {
        String id = createReservation(11, "2026-11-01", "2026-11-03"); // SUITE, 2 nights

        mockMvc.perform(post("/api/hotel/servicios/{reservaId}", id).header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"serviceType\": \"SPA\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/hotel/checkin/{reservaId}", id).header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_IN"));
        mockMvc.perform(put("/api/hotel/checkout/{reservaId}", id).header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_OUT"));

        mockMvc.perform(get("/api/hotel/reserva/{reservaId}", id).header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomTotal").value(360.0)) // 2 x 180.00
                .andExpect(jsonPath("$.servicesTotal").value(50.0))
                .andExpect(jsonPath("$.total").value(410.0))
                .andExpect(jsonPath("$.invoice.total").value(410.0))
                .andExpect(jsonPath("$.invoice.lines", hasSize(2)));
    }

    private String registerAndGetToken(String email) throws Exception {
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\": \"Ada Lovelace\", \"email\": \"" + email
                                + "\", \"password\": \"secret-pass\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.token");
    }

    private String bearer() {
        return "Bearer " + token;
    }

    private String createReservation(int roomId, String checkIn, String checkOut) throws Exception {
        String response = mockMvc.perform(post("/api/hotel/reservar").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationBody(roomId, checkIn, checkOut)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.id");
    }

    private static String reservationBody(int roomId, String checkIn, String checkOut) {
        return """
                {"roomId": %d, "checkIn": "%s", "checkOut": "%s",
                 "guest": {"fullName": "Ada Lovelace", "email": "ada@example.com",
                           "phone": "3001234567", "document": "CC-100"}}
                """.formatted(roomId, checkIn, checkOut);
    }
}

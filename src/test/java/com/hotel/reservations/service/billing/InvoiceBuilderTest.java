package com.hotel.reservations.service.billing;

import com.hotel.reservations.model.Guest;
import com.hotel.reservations.model.Invoice;
import com.hotel.reservations.model.Reservation;
import com.hotel.reservations.model.Room;
import com.hotel.reservations.model.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvoiceBuilderTest {

    private static final LocalDateTime ISSUED_AT = LocalDateTime.of(2026, 11, 3, 12, 0);

    private Reservation reservation;

    @BeforeEach
    void setUp() {
        reservation = new Reservation(new Room(6, "201", RoomType.DOUBLE),
                new Guest("Ada Lovelace", "ada@example.com", "3001234567", "CC-100"),
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3));
    }

    @Test
    void copiesTheReservationDataIntoTheInvoice() {
        Invoice invoice = InvoiceBuilder.forReservation(reservation)
                .number("FAC-0001").issuedAt(ISSUED_AT)
                .addLine("Room", 2, new BigDecimal("90.00"))
                .build();

        assertThat(invoice.number()).isEqualTo("FAC-0001");
        assertThat(invoice.reservationId()).isEqualTo(reservation.getId());
        assertThat(invoice.guestName()).isEqualTo("Ada Lovelace");
        assertThat(invoice.roomNumber()).isEqualTo("201");
        assertThat(invoice.roomType()).isEqualTo(RoomType.DOUBLE);
        assertThat(invoice.checkIn()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(invoice.checkOut()).isEqualTo(LocalDate.of(2026, 11, 3));
        assertThat(invoice.nights()).isEqualTo(2);
        assertThat(invoice.issuedAt()).isEqualTo(ISSUED_AT);
    }

    @Test
    void computesEachLineSubtotalAndTheInvoiceTotals() {
        Invoice invoice = InvoiceBuilder.forReservation(reservation)
                .number("FAC-0001").issuedAt(ISSUED_AT)
                .addLine("Room", 2, new BigDecimal("90.00"))
                .addLine("Spa", 1, new BigDecimal("50.00"))
                .build();

        assertThat(invoice.lines()).hasSize(2);
        assertThat(invoice.lines().get(0).subtotal()).isEqualByComparingTo("180.00");
        assertThat(invoice.lines().get(1).subtotal()).isEqualByComparingTo("50.00");
        assertThat(invoice.subtotal()).isEqualByComparingTo("230.00");
        assertThat(invoice.total()).isEqualByComparingTo("230.00");
        assertThat(invoice.total().scale()).isEqualTo(2);
    }

    @Test
    void keepsTheLinesInTheOrderTheyWereAdded() {
        Invoice invoice = InvoiceBuilder.forReservation(reservation)
                .number("FAC-0001").issuedAt(ISSUED_AT)
                .addLine("First", 1, BigDecimal.ONE)
                .addLine("Second", 1, BigDecimal.ONE)
                .addLine("Third", 1, BigDecimal.ONE)
                .build();

        assertThat(invoice.lines()).extracting("description").containsExactly("First", "Second", "Third");
    }

    @Test
    void requiresTheNumberTheIssueTimeAndAtLeastOneLine() {
        assertThatThrownBy(() -> InvoiceBuilder.forReservation(reservation).issuedAt(ISSUED_AT)
                .addLine("Room", 1, BigDecimal.TEN).build())
                .isInstanceOf(IllegalStateException.class).hasMessage("The invoice number is required");
        assertThatThrownBy(() -> InvoiceBuilder.forReservation(reservation).number("FAC-0001")
                .addLine("Room", 1, BigDecimal.TEN).build())
                .isInstanceOf(IllegalStateException.class).hasMessage("The issue time is required");
        assertThatThrownBy(() -> InvoiceBuilder.forReservation(reservation).number("FAC-0001")
                .issuedAt(ISSUED_AT).build())
                .isInstanceOf(IllegalStateException.class).hasMessage("An invoice needs at least one line");
    }

    @Test
    void rejectsAnInvalidQuantityAndAMissingReservation() {
        InvoiceBuilder builder = InvoiceBuilder.forReservation(reservation);

        assertThatThrownBy(() -> builder.addLine("Room", 0, BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> InvoiceBuilder.forReservation(null)).isInstanceOf(NullPointerException.class);
    }
}

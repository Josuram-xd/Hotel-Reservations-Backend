package com.hotel.reservations.event;

import com.hotel.reservations.model.Invoice;
import com.hotel.reservations.model.Reservation;
import com.hotel.reservations.service.BillingService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Observer: generates the final invoice, with the extra charges, as soon as the check-out happens. */
@Component
public class InvoiceListener {

    private final BillingService billingService;

    public InvoiceListener(BillingService billingService) {
        this.billingService = billingService;
    }

    @EventListener
    public void onCheckedOut(ReservationCheckedOut event) {
        Reservation reservation = event.reservation();
        Invoice invoice = billingService.buildInvoice(reservation);
        reservation.attachInvoice(invoice);
    }
}

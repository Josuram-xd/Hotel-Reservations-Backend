# Design patterns used in the backend

The case study requires the **Facade** pattern. These additional patterns keep the code open to change
and are small enough to explain in a workshop. Each one has focused unit tests.

| Pattern | Where | Why |
|---|---|---|
| Facade (required) | `facade/HotelFacade` | One entry point for `createReservation`, `addService`, `checkIn`, `checkOut`. The controller never talks to the subsystems directly. |
| Strategy | `service/pricing/PricingStrategy`, `LowSeasonStrategy`, `HighSeasonStrategy` | The price of a night depends on the season. A new season rule is a new class, not a new `if`. `RateService` picks the strategy night by night. |
| State | `model/state/ReservationState`, `ConfirmedState`, `CheckedInState`, `CheckedOutState` | Each state decides which actions are legal (`addService`, `checkIn`, `checkOut`, key, invoice) and which state comes next. Illegal transitions raise `INVALID_STATE`. |
| Factory | `service/extras/ExtraServiceFactory` | Builds the right extra (spa, breakfast, transfer) from a `ServiceType` and the length of the stay. `AdditionalServiceService` delegates to it. |
| Builder | `service/billing/InvoiceBuilder` | Assembles the itemized invoice line by line and computes subtotal and total from the lines, so the invoice cannot disagree with them. `BillingService` uses it. |
| Observer | `event/ReservationCheckedIn`, `ReservationCheckedOut` and their listeners (`DigitalKeyListener`, `InvoiceListener`, `ReservationAuditLogger`) | The facade announces the milestone with a Spring event. The digital key and the final invoice are produced by listeners; adding a reaction is adding a listener. |
| Repository | `repository/RoomRepository`, `ReservationRepository`, `UserRepository` | In-memory collections hidden behind classes with intention-revealing methods. |
| Singleton | Spring beans | Services and repositories are single instances managed by the container. |

## How check-in and check-out flow (State + Observer)

```
checkIn(id)   reservation.checkIn()      State: CONFIRMED -> CHECKED_IN
              publish ReservationCheckedIn  -> DigitalKeyListener issues the key
                                            -> ReservationAuditLogger logs it
checkOut(id)  roomService.release(...)   State: CHECKED_IN -> CHECKED_OUT (room is free again)
              publish ReservationCheckedOut -> InvoiceListener builds the invoice with InvoiceBuilder
                                            -> ReservationAuditLogger logs it
```

Listeners run synchronously in the same thread, so the response of the request already contains the key or the invoice.
If a listener fails, the exception reaches the caller and the API answers with an error.

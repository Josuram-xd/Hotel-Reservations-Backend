# Design patterns used in the backend

The case study requires the **Facade** pattern. These additional patterns keep the code open to change
and are small enough to explain in a workshop. Each one maps to a task in `TASKS.md` (Phase 9).

| Pattern | Where | Why |
|---|---|---|
| Facade (required) | `HotelFacade` | One entry point for `createReservation`, `addService`, `checkIn`, `checkOut`. The controller never talks to the subsystems directly. |
| Strategy | `PricingStrategy` with `LowSeasonStrategy` and `HighSeasonStrategy` | Price per night depends on the season. A new season rule is a new class, not a new `if`. `RateService` picks the strategy per night. |
| State | `ReservationState` with `Confirmed`, `CheckedIn`, `CheckedOut` | Each state decides which actions are legal (`addService`, `checkIn`, `checkOut`). Illegal transitions raise `INVALID_STATE`. |
| Factory | `ExtraServiceFactory` | Builds the right extra (spa, breakfast, transfer) from a `ServiceType` and the stay length. |
| Builder | `InvoiceBuilder` | Assembles the itemized invoice (room lines, extras, subtotal, total) step by step. |
| Observer | Spring `ApplicationEventPublisher` with `ReservationCheckedIn` and `ReservationCheckedOut` | Side effects (issue digital key, release room) react to events instead of being hard-wired inside the facade. |
| Repository | `RoomRepository`, `ReservationRepository`, `UserRepository` | In-memory collections hidden behind interfaces. |
| Singleton | Spring beans | Services and repositories are single instances managed by the container. |

Keep each pattern in its own package or sub-package and cover it with a focused unit test.

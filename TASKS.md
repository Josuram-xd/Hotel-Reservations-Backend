# TASKS - Hotel Reservations Backend

Spring Boot 3 / Java 21 / Maven. In-memory state with Java collections.
All code in English. One commit per task, using the task message with its `[x.y]` suffix.
Branch naming: `task-<n>-<short-description>`.

## Business assumptions (not defined in the case study)

- Room types: `SINGLE`, `DOUBLE`, `SUITE`. Base price per night: 60 / 90 / 180 (configurable in `application.yml`).
- High season: Jun 1 - Aug 31 and Dec 15 - Jan 6 (configurable). High = base x 1.5, low = base.
- Season is evaluated per night, so a stay crossing seasons is priced night by night.
- Extras: spa $50 (flat), breakfast $15 per night of stay, transfer $30 (flat).
- Reservation lifecycle: `CONFIRMED` -> `CHECKED_IN` -> `CHECKED_OUT`.
- Digital key (UUID) is generated at check-in. Room is released at check-out.
- Date ranges are `[checkIn, checkOut)`: back-to-back stays do not overlap.

## Phase 0 - Setup

- [x] feat: scaffold Spring Boot project with config, Dockerfile and docs [0.1]
- [x] docs: add AGENTS.md with git workflow rules for AI assistants [0.2]

## Phase 1 - Domain and rooms

- [ ] feat(model): add domain enums, Room, Reservation, Guest and AdditionalService models [1.1]
- [ ] feat(room): seed 15+ predefined rooms across SINGLE, DOUBLE and SUITE types [1.2]
- [ ] feat(room): implement RoomService availability search by date range and type [1.3]
- [ ] feat(room): implement RoomService reserve and release with overlap protection [1.4]
- [ ] test(room): cover RoomService availability, overlap, reserve and release [1.5]

## Phase 2 - Pricing

- [ ] feat(rate): add configurable pricing properties (base prices, seasons, multiplier) [2.1]
- [ ] feat(rate): implement RateService total price by nights, room type and season [2.2]
- [ ] test(rate): cover low season, high season, mixed-season and edge cases [2.3]

## Phase 3 - Extras, billing and access

- [ ] feat(extras): implement AdditionalServiceService (spa, breakfast, transfer) [3.1]
- [ ] feat(billing): implement BillingService detailed invoice with line-item breakdown [3.2]
- [ ] feat(access): implement AccessService digital key generation (UUID) [3.3]
- [ ] test(services): cover extras, billing and access services [3.4]

## Phase 4 - Facade

- [ ] feat(facade): implement HotelFacade.createReservation with validation [4.1]
- [ ] feat(facade): implement HotelFacade.addService (only while CONFIRMED or CHECKED_IN) [4.2]
- [ ] feat(facade): implement HotelFacade.checkIn issuing the digital key [4.3]
- [ ] feat(facade): implement HotelFacade.checkOut computing extra charges and final invoice [4.4]
- [ ] test(facade): cover full lifecycle and invalid state transitions [4.5]

## Phase 5 - REST API

- [ ] feat(api): add request/response DTOs with bean validation [5.1]
- [ ] feat(api): POST /api/hotel/reservar [5.2]
- [ ] feat(api): GET /api/hotel/disponibilidad (checkIn, checkOut, type, optional price preview) [5.3]
- [ ] feat(api): POST /api/hotel/servicios/{reservaId} [5.4]
- [ ] feat(api): PUT /api/hotel/checkin/{reservaId} and PUT /api/hotel/checkout/{reservaId} [5.5]
- [ ] feat(api): GET /api/hotel/reserva/{reservaId} [5.6]
- [ ] feat(api): global exception handler with consistent JSON errors and error codes [5.7]
- [ ] feat(config): configurable CORS via CORS_ALLOWED_ORIGINS [5.8]
- [ ] test(api): MockMvc integration tests for every endpoint, target 80%+ coverage [5.9]

## Phase 6 - Concurrency and hardening

- [ ] feat(room): make reservations thread-safe to prevent double booking [6.1]
- [ ] test(room): concurrent booking test on the same room and dates [6.2]

## Phase 7 - Deployment (Railway)

- [ ] docs: document API contract, assumptions and local run in README [7.1]
- [ ] ci: add GitHub Actions workflow running mvn verify [7.2]
- [ ] chore(deploy): deploy to Railway with PORT and CORS_ALLOWED_ORIGINS variables [7.3]
- [ ] chore(deploy): smoke test production (health, availability, full lifecycle) [7.4]

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

- [x] feat(model): add domain enums, Room, Reservation, Guest and AdditionalService models [1.1]
- [x] feat(room): seed 15+ predefined rooms across SINGLE, DOUBLE and SUITE types [1.2]
- [x] feat(room): implement RoomService availability search by date range and type [1.3]
- [x] feat(room): implement RoomService reserve and release with overlap protection [1.4]
- [x] test(room): cover RoomService availability, overlap, reserve and release [1.5]

## Phase 2 - Pricing

- [x] feat(rate): add configurable pricing properties (base prices, seasons, multiplier) [2.1]
- [x] feat(rate): implement RateService total price by nights, room type and season [2.2]
- [x] test(rate): cover low season, high season, mixed-season and edge cases [2.3]

## Phase 3 - Extras, billing and access

- [x] feat(extras): implement AdditionalServiceService (spa, breakfast, transfer) [3.1]
- [x] feat(billing): implement BillingService detailed invoice with line-item breakdown [3.2]
- [x] feat(access): implement AccessService digital key generation (UUID) [3.3]
- [x] test(services): cover extras, billing and access services [3.4]

## Phase 4 - Facade

- [x] feat(facade): implement HotelFacade.createReservation with validation [4.1]
- [x] feat(facade): implement HotelFacade.addService (only while CONFIRMED or CHECKED_IN) [4.2]
- [x] feat(facade): implement HotelFacade.checkIn issuing the digital key [4.3]
- [x] feat(facade): implement HotelFacade.checkOut computing extra charges and final invoice [4.4]
- [x] test(facade): cover full lifecycle and invalid state transitions [4.5]

## Phase 5 - REST API

- [x] feat(api): add request/response DTOs with bean validation [5.1]
- [x] feat(api): POST /api/hotel/reservar [5.2]
- [x] feat(api): GET /api/hotel/disponibilidad (checkIn, checkOut, type, optional price preview) [5.3]
- [x] feat(api): POST /api/hotel/servicios/{reservaId} [5.4]
- [x] feat(api): PUT /api/hotel/checkin/{reservaId} and PUT /api/hotel/checkout/{reservaId} [5.5]
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

## Phase 8 - Users and authentication (requested by the frontend team)

Contract: see the "Authentication" section of
[`API_CONTRACT.md` in the frontend repo](https://github.com/Josuram-xd/Hotel-Reservations-Frontend/blob/main/docs/API_CONTRACT.md).
Users live in memory like everything else. Passwords are hashed with BCrypt (`spring-security-crypto`, no full Spring Security needed).
Sessions use opaque bearer tokens (random UUID stored in a `ConcurrentHashMap` with expiry), checked by a servlet filter or `HandlerInterceptor`.

- [ ] feat(user): add User model, in-memory UserRepository and unique email rule [8.1]
- [ ] feat(auth): implement AuthService register and login with BCrypt and token issuing [8.2]
- [ ] feat(auth): POST /api/auth/register, POST /api/auth/login, GET /api/auth/me, POST /api/auth/logout [8.3]
- [ ] feat(auth): bearer token interceptor protecting booking endpoints (GET /disponibilidad stays public) [8.4]
- [ ] feat(reservation): link reservations to their owner and return FORBIDDEN for other users [8.5]
- [ ] feat(api): GET /api/hotel/mis-reservas listing the authenticated user reservations [8.6]
- [ ] feat(api): add error codes UNAUTHORIZED, FORBIDDEN, INVALID_CREDENTIALS, EMAIL_ALREADY_REGISTERED [8.7]
- [ ] test(auth): cover register, login, token validation and ownership rules [8.8]

## Phase 9 - Design patterns (see `docs/PATTERNS.md`)

- [ ] refactor(rate): extract PricingStrategy with LowSeasonStrategy and HighSeasonStrategy [9.1]
- [ ] refactor(reservation): model the lifecycle with the State pattern (Confirmed, CheckedIn, CheckedOut) [9.2]
- [ ] feat(extras): ExtraServiceFactory creating spa, breakfast and transfer services [9.3]
- [ ] feat(billing): InvoiceBuilder for the itemized invoice [9.4]
- [ ] feat(events): publish ReservationCheckedIn and ReservationCheckedOut events (Observer) and react to them (key issuing, room release) [9.5]
- [ ] test(patterns): cover each pattern in isolation [9.6]

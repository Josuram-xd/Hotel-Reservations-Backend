# Hotel Reservations - Backend

REST API for a hotel reservation system. Spring Boot 3, Java 21, Maven.
State is kept in memory with Java collections, so **all data resets on every restart**.

The frontend lives in [Hotel-Reservations-Frontend](https://github.com/Josuram-xd/Hotel-Reservations-Frontend).
The request/response contract is mirrored in its `docs/API_CONTRACT.md`.

## Architecture

```
HotelController -> HotelFacade -> RoomService, RateService, AdditionalServiceService, BillingService, AccessService
                                   |
                                   +-> RoomRepository, ReservationRepository (in-memory)
```

- `HotelFacade` is the only entry point used by the controller (Facade pattern, required by the case study).
- 15 predefined rooms: 5 `SINGLE` (101-105), 5 `DOUBLE` (201-205) and 5 `SUITE` (301-305), see `RoomRepository`.
- Concurrency: `RoomService.reserve` is synchronized, so two simultaneous requests cannot double-book a room.

## Business rules and assumptions

The case study does not define everything. These are the rules the code applies (all configurable in `application.yml`):

- Base price per night: `SINGLE` 60, `DOUBLE` 90, `SUITE` 180.
- High season: Jun 1 - Aug 31 and Dec 15 - Jan 6. High season price = base x 1.5.
- The season is evaluated **per night**, so a stay crossing seasons is priced night by night.
- Extras: spa 50 (flat), breakfast 15 per night of the stay, transfer 30 (flat).
- Date ranges are `[checkIn, checkOut)`: a stay that starts the day another one ends does not overlap.
- Lifecycle: `CONFIRMED` -> `CHECKED_IN` -> `CHECKED_OUT`. Services can be added while `CONFIRMED` or `CHECKED_IN`.
- The digital key (UUID) is issued at check-in. The room is released and the final invoice is generated at check-out.

## Endpoints

Dates are ISO `YYYY-MM-DD`. Money values are decimals with 2 digits.

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/hotel/disponibilidad?checkIn=&checkOut=&type=` | Availability with price preview (`type` is optional: `SINGLE`, `DOUBLE`, `SUITE`) |
| POST | `/api/hotel/reservar` | Create a reservation (201) |
| GET | `/api/hotel/reserva/{reservaId}` | Reservation details |
| POST | `/api/hotel/servicios/{reservaId}` | Add an extra service |
| PUT | `/api/hotel/checkin/{reservaId}` | Check in, issues the digital key |
| PUT | `/api/hotel/checkout/{reservaId}` | Check out, final invoice |

### Examples

Create a reservation:

```bash
curl -X POST http://localhost:8080/api/hotel/reservar \
  -H "Content-Type: application/json" \
  -d '{
    "roomId": 1,
    "checkIn": "2026-11-01",
    "checkOut": "2026-11-03",
    "guest": { "fullName": "Ada Lovelace", "email": "ada@example.com", "phone": "3001234567", "document": "123456" }
  }'
```

Add a service (`SPA`, `BREAKFAST` or `TRANSFER`):

```bash
curl -X POST http://localhost:8080/api/hotel/servicios/<reservaId> \
  -H "Content-Type: application/json" -d '{ "serviceType": "SPA" }'
```

Check in and check out:

```bash
curl -X PUT http://localhost:8080/api/hotel/checkin/<reservaId>
curl -X PUT http://localhost:8080/api/hotel/checkout/<reservaId>
```

### Errors

Every error uses the same payload: `{ "status": 409, "code": "ROOM_NOT_AVAILABLE", "message": "...", "details": [...] }`.

| Code | HTTP | When |
|---|---|---|
| `VALIDATION_ERROR` | 400 | Missing or malformed fields (`details` lists each field) |
| `INVALID_DATES` | 400 | `checkOut` is not after `checkIn` |
| `INVALID_SERVICE` | 400 | Unknown service type |
| `ROOM_NOT_FOUND` | 404 | Unknown room id |
| `RESERVATION_NOT_FOUND` | 404 | Unknown reservation id |
| `ROOM_NOT_AVAILABLE` | 409 | The room is already booked for those dates |
| `INVALID_STATE` | 409 | The action is not allowed in the current status |
| `INTERNAL_ERROR` | 500 | Unexpected error |

The frontend maps `code` to Spanish messages, so codes are stable; `message` is for developers.

## Run locally

```bash
mvn spring-boot:run     # http://localhost:8080
mvn test                # unit and integration tests
mvn verify              # what CI runs
```

Health check: `GET /actuator/health`.

## Environment variables

| Name | Default | Purpose |
|---|---|---|
| `PORT` | `8080` | HTTP port (set by Railway) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Comma-separated allowed origins (the frontend URL) |

## Deployment

See [`docs/DEPLOY.md`](docs/DEPLOY.md) for the Railway steps.

## Work plan

`TASKS.md` has the task list and `AGENTS.md` the rules for AI assistants working on this repo.

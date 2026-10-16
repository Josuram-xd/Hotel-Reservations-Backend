# Hotel Reservations - Backend

REST API for a hotel reservation system. Spring Boot 3, Java 21, Maven.
State is kept in memory with Java collections, so data resets on every restart.

## Architecture

`HotelController` -> `HotelFacade` -> `RoomService`, `RateService`, `AdditionalServiceService`, `BillingService`, `AccessService`.

## Endpoints

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/hotel/reservar` | Create a reservation |
| GET | `/api/hotel/disponibilidad` | Search availability by dates and type |
| POST | `/api/hotel/servicios/{reservaId}` | Add an extra service |
| PUT | `/api/hotel/checkin/{reservaId}` | Check in, issues digital key |
| PUT | `/api/hotel/checkout/{reservaId}` | Check out, final invoice |
| GET | `/api/hotel/reserva/{reservaId}` | Reservation details |

See `TASKS.md` for business assumptions and the work plan.

## Run locally

```bash
mvn spring-boot:run
mvn test
```

## Environment variables

| Name | Default | Purpose |
|---|---|---|
| `PORT` | `8080` | HTTP port (set by Railway) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Comma-separated allowed origins (frontend URL) |

## Deployment

Railway builds from the `Dockerfile`. Set `CORS_ALLOWED_ORIGINS` to the Vercel frontend URL.

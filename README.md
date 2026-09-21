# RideLink — IT3130 Application Development Group Assignment

Backend-only microservices for a fictional ride-sharing platform. Demo via **Swagger UI** and the shared **Postman collection**. No frontend is required. Due **01/10/2026**.

## Stack and why

Lab notes and nearby projects did **not** prescribe a stack. The brief says to use the language/framework approved in laboratory sessions; with nothing recorded in this workspace we chose the default below so the group can start immediately.

| Choice | Reason |
| --- | --- |
| **Java 17** (runs on JDK 17+) | Common in IT modules, Spring Boot 3 baseline, matches the assignment “approved during labs” default if the lab used Spring. |
| **Spring Boot 3.4.x** | REST, JPA, validation, security, and Actuator health with little ceremony. |
| **Maven** (parent POM + 4 service modules + `ridelink-common`) | Each service is still an independently runnable Spring Boot app (`-pl <service> -am spring-boot:run`). |
| **H2 file DB per service** | No Docker required for local/CI. Postgres is documented as an optional later swap. |
| **Spring Security + JWT** | Account Service issues tokens; other services validate the same secret and enforce `PASSENGER` / `DRIVER` / `ADMIN`. |
| **springdoc-openapi** | Official demo interface (Swagger UI) on every service. |
| **GitHub Actions** | Builds **and tests** all four services on push/PR. |

This machine currently has **JDK 21**; the compiler target remains **17**. Maven is provided via the **Maven Wrapper** (`mvnw` / `mvnw.cmd`) so a system Maven install is not required.

## Service ownership

Fill names in [`OWNERS.md`](OWNERS.md).

| # | Service | Folder | Port | Swagger UI | Primary owner |
| --- | --- | --- | --- | --- | --- |
| 1 | Account Service | `account-service` | 8081 | http://localhost:8081/swagger-ui.html | Member 1 |
| 2 | Driver & Vehicle Service | `driver-vehicle-service` | 8082 | http://localhost:8082/swagger-ui.html | Member 2 |
| 3 | Ride Management Service | `ride-service` | 8083 | http://localhost:8083/swagger-ui.html | Member 3 |
| 4 | Fare & Payment Service | `fare-payment-service` | 8084 | http://localhost:8084/swagger-ui.html | Member 4 |

API Gateway, Eureka, and a config server are **not** one of the four core services and are not included.

## Prerequisites

- JDK 17 or newer (`java -version`). On Windows, `JAVA_HOME` must point at a real JDK. This repo’s `mvnw.cmd` will try `C:\Program Files\Microsoft\jdk-*` if `JAVA_HOME` is missing or a placeholder.
- Git
- Optional: [Postman](https://www.postman.com/downloads/)
- Optional later: PostgreSQL 16 if you leave H2

## How to run locally

From the repository root (Windows PowerShell):

```powershell
.\mvnw.cmd -pl account-service -am spring-boot:run
.\mvnw.cmd -pl driver-vehicle-service -am spring-boot:run
.\mvnw.cmd -pl ride-service -am spring-boot:run
.\mvnw.cmd -pl fare-payment-service -am spring-boot:run
```

Use four terminals. Start-up **order** (so interservice calls succeed):

1. **Account Service** (8081) — login first
2. **Driver & Vehicle Service** (8082)
3. **Fare & Payment Service** (8084)
4. **Ride Service** (8083) last — it calls 8082 and 8084

Health checks:

- http://localhost:8081/actuator/health
- http://localhost:8082/actuator/health
- http://localhost:8083/actuator/health
- http://localhost:8084/actuator/health

OpenAPI JSON: `/v3/api-docs` on each port.

### Environment variables

Copy [`.env.example`](.env.example) into your IDE run configuration or shell. Spring Boot does **not** load `.env` files automatically. Never commit a real `.env`.

Important variables:

- `JWT_SECRET` — same value on **all four** services (min 32 characters)
- `DRIVER_SERVICE_URL` / `FARE_SERVICE_URL` — used by Ride Service
- `*_SERVER_PORT` and `*_DB_URL` — optional overrides

### Optional Postgres later

Each service already has the PostgreSQL driver on the classpath. Example for Account Service:

```
ACCOUNT_DB_URL=jdbc:postgresql://localhost:5432/ridelink_account
ACCOUNT_DB_USER=ridelink
ACCOUNT_DB_PASSWORD=change-me
ACCOUNT_DB_DRIVER=org.postgresql.Driver
```

Use a **separate database** (or schema) per service. Never share tables across services.

## Sample credentials (seeded)

Password for all demo users: `password`

| Username | Role | Account UUID |
| --- | --- | --- |
| `passenger1` | PASSENGER | `11111111-1111-1111-1111-111111111111` |
| `driver1` | DRIVER | `22222222-2222-2222-2222-222222222222` |
| `admin1` | ADMIN | `33333333-3333-3333-3333-333333333333` |

Driver `driver1` is also seeded in Driver & Vehicle Service (available in **Colombo**, plate `CAB-1234`).

## Tests

```powershell
.\mvnw.cmd -q test
```

CI equivalent: `./mvnw -B verify` (see [`.github/workflows/ci.yml`](.github/workflows/ci.yml)).

## Architecture sketch

```
Passenger / Driver / Admin  →  Swagger or Postman
        │
        ├── Account Service (8081)  own H2  ── issues JWT
        ├── Driver & Vehicle (8082) own H2  ── validates JWT
        ├── Ride Service (8083)     own H2  ── validates JWT
        └── Fare & Payment (8084)   own H2  ── validates JWT

Ride Service  --sync REST-->  Driver & Vehicle  GET /api/drivers/eligible
Ride Service  --sync REST-->  Fare & Payment    POST /api/fares/final
Fare & Payment --sync REST-->  Ride Service      POST /api/rides/{id}/payment
```

Details, data ownership, and a happy-path sequence: [`docs/architecture.md`](docs/architecture.md).  
Viva one-pager: [`docs/viva-cheat-sheet.md`](docs/viva-cheat-sheet.md).  
What each member runs: [`docs/member-guide.md`](docs/member-guide.md).

## Interservice communication (LO2)

Both implemented interactions are **synchronous REST** via Spring `RestClient`, with base URLs from the environment.

| Interaction | Why REST (sync) | Alternative considered |
| --- | --- | --- |
| Ride → Driver: eligible drivers | Assignment needs the list **before** the HTTP response. Immediate consistency. | Async queue would delay assignment and complicate the demo. |
| Ride → Fare: final fare on complete | Completion wants a fare in the same user action. | Async “fare calculated” event is a reasonable later enhancement if fare calculation becomes slow/retryable. |
| Fare → Ride: attach `paymentId` | Payment success should be visible on the ride immediately. | Async “payment completed” event if payment retries become common. |

If Fare Service is down on complete, the ride is still marked **COMPLETED** and `fareNote` records the failure (negative path). If no eligible driver exists, assign returns **409** `NO_DRIVER_AVAILABLE`.

## Fare formula

```
total = base + (distanceKm * perKm) + (durationMin * perMin)
```

Defaults (LKR): `base = 150`, `perKm = 80`, `perMin = 5`.

Distance is **simulated** from pickup/destination strings (no live maps):

```
distanceKm = 2.00 + ((hash(pickup|destination) % 1800) / 100.0)   →  2.00 .. 19.99 km
durationMin = distanceKm / 0.5   # ~30 km/h
```

Configurable in Fare Service `ridelink.fare.*`.

## Ride lifecycle

```
REQUESTED → ASSIGNED → ACCEPTED → IN_PROGRESS → COMPLETED
     └──────────── any of the first four ─────────→ CANCELLED
```

Invalid transitions return `400` with code `INVALID_TRANSITION`.

## Assignment rule

1. Ride Service calls Driver & Vehicle Service for eligible available drivers at the pickup.
2. Eligibility: `available == true`, vehicle plate present, and pickup related to `serviceArea` / `locationLabel` (contains, case-insensitive).
3. Drivers are ordered by a **dummy distance score** (string hash, not GPS).
4. Default: **first eligible** (nearest dummy). Optional body `driverProfileId` must still be in that list.

## Error body (all services)

```json
{
  "timestamp": "2026-09-20T09:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_ERROR",
  "message": "username: must not be blank",
  "path": "/api/accounts/register",
  "details": ["username: must not be blank"]
}
```

## Secrets policy

- Do **not** commit passwords, JWT secrets for shared/prod, connection strings, or tokens.
- Local defaults in `application.yml` are **dev-only** placeholders so the scaffold boots without extra setup.
- For submission/demo machines, set `JWT_SECRET` via environment variables and rotate it.
- `.env` is gitignored; only `.env.example` is tracked.

## Git workflow

See [`CONTRIBUTING.md`](CONTRIBUTING.md). Short version: feature branches, pull requests, one owner per service, no direct commits to `main` for feature work.

## Implemented vs TODO (7 required workflows)

| # | Workflow | Scaffold status |
| --- | --- | --- |
| 1 | Account and access | **Working:** register, login+JWT, `/me`, profile update, admin status, role checks, seeded users |
| 2 | Driver preparation | **Working:** driver upsert vehicle/availability/location; seeded `driver1` |
| 3 | Fare estimation | **Working:** `POST /api/fares/estimate` with documented formula |
| 4 | Ride request and assignment | **Working:** create ride; assign calls driver service (or 409 if none) |
| 5 | Ride lifecycle | **Working:** accept / start / complete / cancel with transition validation |
| 6 | Completion and payment | **Working:** complete calls fare service; simulated payment + receipt; successful pay attaches `paymentId` onto the ride (Fare → Ride REST). If Ride is down, payment still saves. |
| 7 | Negative scenarios | **Working:** no driver, invalid transition, unauthorised/invalid token, simulated payment fail (`cardLast4=0000`). Capture Postman/CI evidence for the report. |

## Suggested next implementation order

1. Fill `OWNERS.md` and create the shared GitHub repo; turn on Actions.
2. Member 1: extra account tests (duplicate username, suspend then login) and admin listing if desired.
3. Member 2: more than one seeded driver / unavailable driver for demo negatives; maybe a PATCH for availability only.
4. Member 3: persist assignment attempts, handle driver-service downtime explicitly, optional async “driver offered ride” later if the group wants an LO2 contrast.
5. Member 4: receipt wording / optional PDF later; payment already links onto the ride.
6. Group: run the Postman collection end-to-end, screenshots for the report, add a release tag at submission.

## Assignment rule reminder

Exactly **four** business microservices; independent persistence; REST JSON; JWT roles; CI; Git history with PRs. An API gateway does not count as a core service. Declare any generative-AI use in the report appendix per the brief.

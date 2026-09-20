# RideLink architecture

IT3130 RideLink is four independently executable Spring Boot services. Each service owns its database. Clients are Swagger UI and Postman. There is no frontend and no API gateway in this scaffold.

## Services and data ownership

```mermaid
flowchart LR
  Client["Swagger / Postman"]
  Acc["Account Service\n:8081\naccounts H2"]
  Drv["Driver and Vehicle Service\n:8082\ndriver_profiles H2"]
  Ride["Ride Service\n:8083\nrides H2"]
  Fare["Fare and Payment Service\n:8084\nfare_quotes + payments H2"]

  Client --> Acc
  Client --> Drv
  Client --> Ride
  Client --> Fare
  Ride -->|"GET eligible drivers\nsync REST + JWT"| Drv
  Ride -->|"POST final fare\nsync REST + JWT"| Fare
```

| Service | Owns (never queried by others) | Identifiers it mints | Identifiers it may store as foreign *references* |
| --- | --- | --- | --- |
| Account | `accounts` (credentials, role, status, profile) | `accountId` | — |
| Driver & Vehicle | `driver_profiles` (vehicle, availability, area, location) | `driverProfileId` | `accountId` (from JWT, not joined to Account DB) |
| Ride | `rides` (request, assignment, lifecycle) | `rideId` | `passengerAccountId`, `driverAccountId`, `driverProfileId`, `fareId`, `paymentId` |
| Fare & Payment | `fare_quotes`, `payments` | `fareId`, `paymentId` | `rideId`, `accountId` |

Storing another service’s UUID is allowed. Opening that service’s tables is not.

## JWT

Account Service signs HS256 tokens:

- `sub` = account UUID
- `username`
- `role` = `PASSENGER` | `DRIVER` | `ADMIN`
- `exp`

Other services share `ridelink-common` (`JwtAuthFilter` + `JwtService`) and the same `JWT_SECRET`.

Public paths: health, OpenAPI/Swagger, and Account `register`/`login`.

## Communication choices

**Synchronous REST** for both required interservice calls.

1. **Ride → Driver** `GET /api/drivers/eligible?pickup=`  
   Assignment cannot finish without a current availability snapshot. REST keeps the workflow in one request/response. A message queue would add eventual consistency the demo does not need.

2. **Ride → Fare** `POST /api/fares/final`  
   Completion wants a fare immediately. If Fare Service is down, Ride still transitions to `COMPLETED` and records `fareNote` (graceful degradation).

**Optional later (async):** after `COMPLETED`, publish `RideCompleted` so Fare Service can retry independently, or notify the driver of a new assignment. That would be a good LO2 contrast in the report; it is not required for the scaffold.

gRPC was considered for the eligible-driver call (typed contract, slightly lower latency) and rejected for this assignment: all official clients are HTTP (Swagger/Postman), and adding protobuf would not improve marks unless the group can explain it in the viva.

## Ride lifecycle

Valid transitions (enforced in `RideStatus`):

```
REQUESTED → ASSIGNED → ACCEPTED → IN_PROGRESS → COMPLETED
REQUESTED | ASSIGNED | ACCEPTED | IN_PROGRESS → CANCELLED
COMPLETED and CANCELLED are terminal
```

## Happy-path sequence

Passenger `passenger1`, driver `driver1`, pickup `Colombo Fort`, destination `Kandy`.

```mermaid
sequenceDiagram
  actor P as Passenger
  participant A as Account :8081
  participant D as Driver :8082
  participant R as Ride :8083
  participant F as Fare :8084

  P->>A: POST /api/accounts/login
  A-->>P: JWT
  P->>D: (driver already seeded available in Colombo)
  P->>F: POST /api/fares/estimate
  F-->>P: quote (formula)
  P->>R: POST /api/rides
  R-->>P: ride REQUESTED
  P->>R: POST /api/rides/{id}/assign
  R->>D: GET /api/drivers/eligible?pickup=Colombo Fort
  D-->>R: [driver1]
  R-->>P: ride ASSIGNED
  Note over R: Driver logs in and accepts
  R-->>P: ACCEPTED then IN_PROGRESS then COMPLETED
  R->>F: POST /api/fares/final
  F-->>R: final total
  P->>F: POST /api/payments
  F-->>P: COMPLETED + receiptNumber
```

## Monolith comparison (report seed)

A modular monolith would share one deployable and one database, with simpler transactions and one JWT filter. RideLink uses four services because the brief requires independent development, clear business boundaries, and future scaling. Cost: distributed failure modes, duplicated JWT config, and no cross-table joins. Those costs are accepted and mitigated with timeouts, explicit UUIDs, and a shared error body.

## Folder structure

```
Pro/
  pom.xml
  ridelink-common/          shared JWT, error body, exceptions
  account-service/
  driver-vehicle-service/
  ride-service/
  fare-payment-service/
  docs/architecture.md
  postman/
  .github/workflows/ci.yml
```

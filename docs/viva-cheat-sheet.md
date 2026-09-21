# RideLink viva cheat sheet (one page)

**Repo:** https://github.com/IT24104300/RideLink-AD-.git  
**Demo clients:** Swagger / Postman (no frontend required). Use Chrome with `127.0.0.1`, not Cursor Simple Browser.

| Service | Owner | Port | Swagger |
| --- | --- | --- | --- |
| Account | Member 1 | 8081 | http://127.0.0.1:8081/swagger-ui.html |
| Driver & Vehicle | Member 2 | 8082 | http://127.0.0.1:8082/swagger-ui.html |
| Ride | Member 3 | 8083 | http://127.0.0.1:8083/swagger-ui.html |
| Fare & Payment | Member 4 | 8084 | http://127.0.0.1:8084/swagger-ui.html |

**Seeded users** (password `password`): `passenger1`, `driver1`, `admin1`. Driver1 is available in **Colombo**.

## Say this if asked “what did we build?”

Four independently runnable Spring Boot services. Each owns its own H2 database. Account issues JWT; the others validate the same secret and roles `PASSENGER` / `DRIVER` / `ADMIN`. Official UI is Swagger, not a taxi website.

## Rules to recite

- **Fare:** `total = 150 + (km × 80) + (min × 5)` LKR. Distance is simulated from pickup/destination strings (hash), not Google Maps.
- **Assign:** Ride calls Driver `GET /api/drivers/eligible?pickup=`. Eligible = available + plate + pickup matches service area. Pick first / dummy-nearest.
- **Lifecycle:** `REQUESTED → ASSIGNED → ACCEPTED → IN_PROGRESS → COMPLETED`, plus `CANCELLED`. Invalid → `400 INVALID_TRANSITION`.
- **Data:** store another service’s UUID; **never** query another service’s tables.
- **Comms (sync REST):** Ride→Driver (need list before assign returns); Ride→Fare (need fare on complete); Fare→Ride (attach `paymentId` after pay). Async queue was considered and deferred.
- **Failures:** no driver → `409 NO_DRIVER_AVAILABLE`. Fare down on complete → ride still `COMPLETED` + `fareNote`. Ride down on pay → payment still saved. `cardLast4=0000` → payment `FAILED`.

## Why not a monolith / MongoDB / pretty UI?

Brief requires four business services and independent persistence. A monolith is simpler (one DB, one deploy) but fails the brief. H2 is enough; Postgres later with **four** databases. MongoDB is not required. Swagger is the marked demo UI.

## Git / CI

One repo. Feature branch → PR → review → merge. CI: `./mvnw -B verify` (all four modules). Own Git identity. Fill `OWNERS.md`.

## Demo script (about 4 minutes)

1. Login `passenger1` on 8081; Authorize. Estimate on 8084 (Colombo Fort → Kandy).
2. Create ride + assign on 8083.
3. Login `driver1`; accept → start → complete.
4. Login `passenger1`; pay on 8084 (`cardLast4` `4242`). GET ride: `paymentId` set.
5. Negatives: no token 401; assign pickup `Jaffna` 409; complete a `REQUESTED` ride 400.

Each member must explain **the whole system** and **their** controller, entity, tests, and any REST client they own.

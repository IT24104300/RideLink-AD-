Ride Management Service — Member 3. Port 8083.

Folder: `services/ride-service`. From the repo root: `.\mvnw.cmd -pl services/ride-service -am spring-boot:run`

Owns ride lifecycle and calls Driver (eligible) and Fare (final) over sync REST. See the root README and `docs/architecture.md`.

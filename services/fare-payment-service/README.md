Fare & Payment Service — Member 4. Port 8084.

Folder: `services/fare-payment-service`. From the repo root: `.\mvnw.cmd -pl services/fare-payment-service -am spring-boot:run`

Formula: `base + (distanceKm * perKm) + (durationMin * perMin)`. Simulated payments fail when `cardLast4=0000` or `simulateFailure=true`. See the root README.

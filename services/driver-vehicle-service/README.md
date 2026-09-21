Driver & Vehicle Service — Member 2. Port 8082.

Folder: `services/driver-vehicle-service`. From the repo root: `.\mvnw.cmd -pl services/driver-vehicle-service -am spring-boot:run`

Authenticated drivers upsert vehicle/availability/location. Ride Service calls `GET /api/drivers/eligible`. See the root README.

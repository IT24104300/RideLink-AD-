# RideLink — commands for the four members

Use this to brief the group. Everyone clones the **same** repo. Do not create four GitHub repositories.

```powershell
git clone https://github.com/IT24104300/RideLink-AD-.git
cd RideLink-AD-
```

On this Windows PC:

```powershell
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.11.10-hotspot"
```

Start **Account → Driver → Fare → Ride** (four terminals, from repo root).

```powershell
.\mvnw.cmd -pl services/account-service -am spring-boot:run
.\mvnw.cmd -pl services/driver-vehicle-service -am spring-boot:run
.\mvnw.cmd -pl services/fare-payment-service -am spring-boot:run
.\mvnw.cmd -pl services/ride-service -am spring-boot:run
```

macOS / Linux: `./mvnw` instead of `.\mvnw.cmd`.

Tests (whole project):

```powershell
.\mvnw.cmd -q test
```

One member’s tests only:

```powershell
.\mvnw.cmd -pl services/account-service -am test
.\mvnw.cmd -pl services/driver-vehicle-service -am test
.\mvnw.cmd -pl services/ride-service -am test
.\mvnw.cmd -pl services/fare-payment-service -am test
```

## IntelliJ (any member)

1. File → Open → root `pom.xml`.
2. Project SDK = JDK 17 or 21.
3. Run the main class for **your** service (do not run the parent POM).
4. Open Swagger in Chrome: `http://127.0.0.1:<port>/swagger-ui.html`.

## Git (every member, own GitHub login)

```powershell
git checkout main
git pull
git checkout -b feature/<your-service>/<short-name>
# edit your folder
git add <your-service>
git commit -m "Describe why you changed it."
git push -u origin HEAD
```

Then open a pull request on GitHub. Another member reviews. Do not commit secrets or `target/`.

---

## Member 1 — Account Service (8081)

**Folder:** `services/account-service`  
**Main class:** `com.ridelink.account.AccountServiceApplication`  
**Swagger:** http://127.0.0.1:8081/swagger-ui.html  

You own registration, login, JWT, roles, profile, account status.

```powershell
.\mvnw.cmd -pl services/account-service -am spring-boot:run
.\mvnw.cmd -pl services/account-service -am test
```

**Swagger you run in the demo**

1. `POST /api/accounts/login`

```json
{ "username": "passenger1", "password": "password" }
```

2. Copy `accessToken` → **Authorize**.
3. `GET /api/accounts/me`
4. Optional: `POST /api/accounts/register` (role `PASSENGER` or `DRIVER` only).
5. Login as `admin1` → `PATCH /api/accounts/{id}/status` with `{ "status": "SUSPENDED" }`.

**Study:** JWT claims (`sub`, `username`, `role`); BCrypt passwords; why other services do not read the account database.

---

## Member 2 — Driver & Vehicle Service (8082)

**Folder:** `services/driver-vehicle-service`  
**Main class:** `com.ridelink.driver.DriverVehicleServiceApplication`  
**Swagger:** http://127.0.0.1:8082/swagger-ui.html  

You own vehicle, availability, service area, simulated location, eligible-driver list.

```powershell
.\mvnw.cmd -pl services/driver-vehicle-service -am spring-boot:run
.\mvnw.cmd -pl services/driver-vehicle-service -am test
```

**Swagger you run in the demo** (Authorize with **driver1** token from 8081)

1. `GET /api/drivers/me` — seeded Colombo / `CAB-1234`.
2. `PUT /api/drivers/me`

```json
{
  "displayName": "Demo Driver",
  "vehicleMake": "Toyota",
  "vehicleModel": "Prius",
  "vehiclePlate": "CAB-1234",
  "vehicleColor": "White",
  "available": true,
  "serviceArea": "Colombo",
  "locationLabel": "Colombo Fort",
  "latitude": 6.93,
  "longitude": 79.85
}
```

3. Passenger token: `GET /api/drivers/eligible?pickup=Colombo Fort`  
4. Negative: `available: false` then Ride assign should fail, or pickup `Jaffna`.

**Study:** eligibility rule (available + plate + area match); dummy distance score; Ride Service calls this API — you do not open Ride’s DB.

---

## Member 3 — Ride Service (8083)

**Folder:** `services/ride-service`  
**Main class:** `com.ridelink.ride.RideServiceApplication`  
**Swagger:** http://127.0.0.1:8083/swagger-ui.html  

You own ride request, assignment, lifecycle, and storing `fareId` / `paymentId` as references.

```powershell
.\mvnw.cmd -pl services/ride-service -am spring-boot:run
.\mvnw.cmd -pl services/ride-service -am test
```

**Swagger you run in the demo**

Passenger token:

```text
POST /api/rides
{ "pickup": "Colombo Fort", "destination": "Kandy" }

POST /api/rides/{id}/assign
{}
```

Driver token:

```text
POST /api/rides/{id}/accept
POST /api/rides/{id}/start
POST /api/rides/{id}/complete
```

Then `GET /api/rides/{id}` — after pay, `paymentId` should be filled (Fare Service calls `POST /api/rides/{id}/payment`).

Negatives: assign with pickup `Jaffna` → `409`; complete a `REQUESTED` ride → `400 INVALID_TRANSITION`.

**Study:** state machine; Ride→Driver and Ride→Fare REST clients; if Fare is down, complete still succeeds with `fareNote`.

---

## Member 4 — Fare & Payment Service (8084)

**Folder:** `services/fare-payment-service`  
**Main class:** `com.ridelink.fare.FarePaymentServiceApplication`  
**Swagger:** http://127.0.0.1:8084/swagger-ui.html  

You own fare estimate/final, simulated payment, receipt, and notifying Ride of `paymentId`.

```powershell
.\mvnw.cmd -pl services/fare-payment-service -am spring-boot:run
.\mvnw.cmd -pl services/fare-payment-service -am test
```

**Swagger you run in the demo** (passenger token)

```text
POST /api/fares/estimate
{ "pickup": "Colombo Fort", "destination": "Kandy" }

POST /api/payments
{
  "rideId": "<from ride>",
  "fareId": "<from complete>",
  "amount": 850.00,
  "currency": "LKR",
  "method": "CARD",
  "cardLast4": "4242"
}

GET /api/payments/{id}/receipt
```

Fail: `"cardLast4": "0000"` → status `FAILED`.

**Study:** formula `base + (km × 80) + (min × 5)`; simulated distance; Fare→Ride attach payment; payments are fake (no real cards).

---

## Happy-path order (who clicks what)

1. **All four** services running.  
2. **Member 1:** login `passenger1`, copy token.  
3. **Member 4:** estimate (Authorize passenger).  
4. **Member 3:** create ride + assign (passenger).  
5. **Member 1:** login `driver1`, copy new token.  
6. **Member 3:** accept → start → complete (driver).  
7. **Member 1:** login `passenger1` again.  
8. **Member 4:** pay `4242`.  
9. **Member 3:** GET ride — `paymentId` present.  
10. Group: one 401, one 409, one failed payment.

Tell teammates: **Authorize** is per tab. Switching passenger/driver means login again and paste the new token. Pickup must include **Colombo** for the seeded driver.

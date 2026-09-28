/**
 * Generates docs/RideLink_Viva_Presentation_Guide.pdf
 *
 *   npm install pdfkit --prefix "$env:TEMP\ridelink-pdf"
 *   $env:NODE_PATH = "$env:TEMP\ridelink-pdf\node_modules"
 *   node docs/generate_viva_pdf.js
 *
 * Canonical source is docs/generate_viva_pdf.py (run that when Python + fpdf2 are installed).
 */
const fs = require("fs");
const path = require("path");
const PDFDocument = require("pdfkit");

const out = path.join(__dirname, "RideLink_Viva_Presentation_Guide.pdf");
const doc = new PDFDocument({
  size: "A4",
  margin: 46,
  bufferPages: true,
  info: {
    Title: "RideLink IT3130 Viva and Demo Playbook",
    Author: "RideLink group",
  },
});
const stream = fs.createWriteStream(out);
doc.pipe(stream);

const navy = "#142d5a";
const blue = "#1e5a8c";
const body = "#1e1e1e";
const qcol = "#782828";
const acol = "#194628";
const W = () => doc.page.width - doc.page.margins.left - doc.page.margins.right;

function footer() {
  const range = doc.bufferedPageRange();
  for (let i = 0; i < range.count; i++) {
    doc.switchToPage(range.start + i);
    doc.fontSize(8).fillColor("#777").text(
      `Page ${i + 1} of ${range.count}  |  github.com/IT24104300/RideLink-AD-`,
      doc.page.margins.left,
      doc.page.height - 32,
      { align: "center", width: W() }
    );
  }
}

function ensure(n = 90) {
  if (doc.y > doc.page.height - n) doc.addPage();
}

function h1(t) {
  ensure(70);
  doc.moveDown(0.25);
  doc.font("Helvetica-Bold").fontSize(15).fillColor(navy).text(t);
  doc.moveDown(0.2);
}

function h2(t) {
  ensure(60);
  doc.moveDown(0.25);
  doc.font("Helvetica-Bold").fontSize(12.5).fillColor(blue).text(t);
  doc.moveDown(0.12);
}

function h3(t) {
  ensure(50);
  doc.moveDown(0.18);
  doc.font("Helvetica-Bold").fontSize(11).fillColor("#222").text(t);
  doc.moveDown(0.08);
}

function p(t) {
  doc.font("Helvetica").fontSize(10).fillColor(body).text(t, { align: "justify", lineGap: 1.4 });
  doc.moveDown(0.22);
}

function bullet(t) {
  doc.font("Helvetica").fontSize(10).fillColor(body).text("•  " + t, { indent: 6, lineGap: 1.15 });
}

function code(t) {
  ensure(80);
  const pad = 8;
  const width = W();
  doc.font("Courier").fontSize(8).fillColor("#111");
  const h = doc.heightOfString(t, { width: width - pad * 2 }) + pad * 2;
  if (doc.y + h > doc.page.height - 50) doc.addPage();
  const y = doc.y;
  doc.save();
  doc.rect(doc.page.margins.left, y, width, h).fill("#f4f5f7");
  doc.restore();
  doc.fillColor("#111").text(t, doc.page.margins.left + pad, y + pad, { width: width - pad * 2 });
  doc.y = y + h + 8;
  doc.x = doc.page.margins.left;
}

function say(t) {
  doc.font("Helvetica-Oblique").fontSize(10).fillColor(blue).text('SAY: "' + t + '"', { lineGap: 1.3 });
  doc.moveDown(0.28);
}

function qa(q, a) {
  ensure(70);
  doc.font("Helvetica-Bold").fontSize(10).fillColor(qcol).text("Q: " + q, { lineGap: 1.15 });
  doc.font("Helvetica").fontSize(10).fillColor(acol).text("A: " + a, { lineGap: 1.15 });
  doc.moveDown(0.3);
}

function table(rows) {
  const left = doc.page.margins.left;
  const col1 = 118;
  const col2 = W() - col1;
  rows.forEach((row, i) => {
    ensure(36);
    const [k, v] = row;
    doc.font("Helvetica-Bold").fontSize(9);
    const h1 = doc.heightOfString(k, { width: col1 - 10 }) + 10;
    doc.font("Helvetica").fontSize(9);
    const h2 = doc.heightOfString(v, { width: col2 - 10 }) + 10;
    const h = Math.max(h1, h2, 18);
    const y = doc.y;
    doc.save();
    doc.rect(left, y, col1, h).fill(i % 2 === 0 ? "#e8eef7" : "#ffffff");
    doc.rect(left + col1, y, col2, h).fill(i % 2 === 0 ? "#e8eef7" : "#ffffff");
    doc.restore();
    doc.strokeColor("#c5d0e0").rect(left, y, col1, h).stroke();
    doc.rect(left + col1, y, col2, h).stroke();
    doc.font("Helvetica-Bold").fontSize(9).fillColor(navy).text(k, left + 5, y + 5, { width: col1 - 10 });
    doc.font("Helvetica").fontSize(9).fillColor(body).text(v, left + col1 + 5, y + 5, { width: col2 - 10 });
    doc.y = y + h;
    doc.x = left;
  });
  doc.moveDown(0.35);
}

// Cover
doc.rect(0, 0, doc.page.width, 92).fill(navy);
doc.fillColor("white").font("Helvetica-Bold").fontSize(11)
  .text("FACULTY OF COMPUTING  |  Department of Information Technology", 46, 22);
doc.fontSize(16).text("IT3130  Application Development", 46, 40);
doc.font("Helvetica").fontSize(10)
  .text("Group viva, live Swagger demo, and per-member commands", 46, 62);

doc.fillColor(navy).font("Helvetica-Bold").fontSize(28)
  .text("RideLink", 46, 116, { align: "center", width: W() });
doc.fontSize(13).text("Viva & Demo Playbook  (four members)", { align: "center", width: W() });
doc.moveDown(0.6);
doc.font("Helvetica").fontSize(10).fillColor(body).text(
  "Print this PDF. Each member keeps their own section open. There is no website. Swagger UI is the official client in the brief. All commands below were verified against this repository.",
  { align: "center", width: W() }
);
doc.moveDown(0.5);
table([
  ["Module", "IT3130 Application Development"],
  ["Assessment", "Group assignment 30%  +  scheduled demonstration / viva"],
  ["Due", "01/10/2026"],
  ["Repo", "https://github.com/IT24104300/RideLink-AD-"],
  ["Stack", "Java 17  |  Spring Boot 3.4  |  Maven Wrapper  |  H2 per service"],
  ["Demo UI", "Chrome  http://127.0.0.1:PORT/swagger-ui.html"],
  ["Users", "passenger1  /  driver1  /  admin1     password = password"],
]);
h3("Who does what on the viva");
table([
  ["Member 1", "Account Service   port 8081   login, JWT, /me"],
  ["Member 2", "Driver & Vehicle  port 8082   profile, eligible list"],
  ["Member 3", "Ride Management   port 8083   create, assign, lifecycle (the story)"],
  ["Member 4", "Fare & Payment    port 8084   estimate, pay 4242, receipt"],
]);
doc.font("Helvetica").fontSize(9).fillColor("#777").text(
  "Not leaked exam papers. These are the clicks, commands, and JSON bodies a marker expects if they grade architecture, REST, JWT, tests, Git, and a live Swagger demo."
);

doc.addPage();
h1("1. What to do on the viva day");
p("It is a group presentation AND an individual viva. Show the running APIs in Chrome Swagger, then each person is questioned. A taxi website is not required. If they ask 'where is the UI?', answer: Swagger is the UI the brief asked for.");
h3("Suggested 12-15 minute flow");
bullet("0:00-1:00   All four Java processes already running. One laptop, Chrome, four tabs.");
bullet("1:00-2:30   Any member (often Member 3) explains: four services, four DBs, JWT.");
bullet("2:30-5:00   Member 1  Account: login, Authorize, GET /me.");
bullet("5:00-7:00   Member 2  Driver: GET /me, eligible?pickup=Colombo Fort.");
bullet("7:00-10:00  Member 3  Ride: create, assign, accept, start, complete.");
bullet("10:00-12:00 Member 4  Fare estimate, pay 4242, receipt.");
bullet("12:00-13:30 One negative: 401, 409 Jaffna, 400 REQUESTED-complete, or card 0000.");
bullet("13:30+      Individual questions. Everyone must explain the WHOLE system.");
h3("Rules while presenting");
bullet("Use Chrome or Edge. Do not use Cursor Simple Browser.");
bullet("Pickup must include the word Colombo so seeded driver1 is eligible.");
bullet("Switching passenger/driver means LOGIN AGAIN and Authorize with the NEW token.");
bullet("Swagger Authorize: paste the token only. Do not type the word Bearer.");
bullet("Authorize is per browser tab. Repeat it on 8082, 8083 and 8084.");

h1("2. Start everything BEFORE they sit down");
p("From the repository root in PowerShell. Four terminals. Order: Account, Driver, Fare, then Ride.");
code(
  "$env:JAVA_HOME = \"C:\\Program Files\\Microsoft\\jdk-21.0.11.10-hotspot\"\n" +
  "cd C:\\Users\\MSI\\Desktop\\Pro\n\n" +
  ".\\mvnw.cmd -pl services/account-service -am spring-boot:run\n" +
  ".\\mvnw.cmd -pl services/driver-vehicle-service -am spring-boot:run\n" +
  ".\\mvnw.cmd -pl services/fare-payment-service -am spring-boot:run\n" +
  ".\\mvnw.cmd -pl services/ride-service -am spring-boot:run"
);
p('Wait until each terminal prints Started ...Application. Then check health (expect {"status":"UP"}):');
code(
  "http://127.0.0.1:8081/actuator/health\n" +
  "http://127.0.0.1:8082/actuator/health\n" +
  "http://127.0.0.1:8083/actuator/health\n" +
  "http://127.0.0.1:8084/actuator/health"
);
h3("IntelliJ IDEA (same for every member)");
bullet("File -> Open -> the root pom.xml (whole RideLink project, not one service folder).");
bullet("Project SDK = JDK 17 or 21. Do NOT run the parent module.");
bullet("Run only YOUR main class:");
code(
  "Member 1   com.ridelink.account.AccountServiceApplication\n" +
  "Member 2   com.ridelink.driver.DriverVehicleServiceApplication\n" +
  "Member 3   com.ridelink.ride.RideServiceApplication\n" +
  "Member 4   com.ridelink.fare.FarePaymentServiceApplication"
);
p("Tests before the viva (whole project):");
code(".\\mvnw.cmd -q test");
h2("Opening lines (any member, 60 seconds)");
say(
  "RideLink is a fictional ride-sharing backend. We split it into four Spring Boot microservices in Java: Account, Driver and Vehicle, Ride, and Fare and Payment. Each service owns its own H2 database. Account issues a JWT; the others validate the same secret and enforce passenger, driver, and admin roles. We demo with Swagger, because the brief is backend-only."
);

doc.addPage();
h1("3. Member 1 — Account Service (8081)");
table([
  ["Folder", "services/account-service"],
  ["Main class", "com.ridelink.account.AccountServiceApplication"],
  ["Swagger", "http://127.0.0.1:8081/swagger-ui.html"],
  ["Open in IDE", "AccountController.java   AccountService.java   AccountServiceTest.java"],
]);
h3("Commands");
code(
  ".\\mvnw.cmd -pl services/account-service -am spring-boot:run\n" +
  ".\\mvnw.cmd -pl services/account-service -am test"
);
h3("What you own");
bullet("POST /api/accounts/register  — passenger or driver only (ADMIN self-register is rejected).");
bullet("POST /api/accounts/login     — returns accessToken (JWT).");
bullet("GET / PUT /api/accounts/me   — profile.");
bullet("Admin: GET /api/accounts/{id}   PATCH /api/accounts/{id}/status");
h3("What you say (45 seconds)");
say(
  "I own Account Service. Passwords are stored as BCrypt hashes, never plaintext. Login signs a JWT with the account UUID, username, and role. Other services never open my database; they only read the token."
);
h3("Code / JSON you apply in Swagger");
p("1. POST /api/accounts/login");
code('{ "username": "passenger1", "password": "password" }');
p("Copy accessToken. Click Authorize (green lock). Paste the token. Do not type Bearer.");
p("2. GET /api/accounts/me   — must return passenger1.");
p("3. Optional register (role PASSENGER or DRIVER only):");
code(
  "{\n" +
  '  "username": "newpass1",\n' +
  '  "password": "password1",\n' +
  '  "email": "newpass1@ridelink.local",\n' +
  '  "fullName": "New Passenger",\n' +
  '  "phone": "0771111111",\n' +
  '  "role": "PASSENGER"\n' +
  "}"
);
p("4. Negative: clear Authorize, GET /api/accounts/me  ->  401 UNAUTHORIZED.");
p("5. Optional admin: login as admin1, then PATCH status:");
code('{ "status": "SUSPENDED" }');
h3("Likely questions");
qa("Why JWT instead of sessions?", "Four separate apps. A session cookie on one server is not trusted by the others. A signed token is validated locally with the shared secret.");
qa("What is in the token?", "sub = account UUID, username, role (PASSENGER / DRIVER / ADMIN), expiry. Signed HS256.");
qa("Can a passenger register as ADMIN?", "No. Self-registration of ADMIN is rejected. Admin accounts are seeded.");

doc.addPage();
h1("4. Member 2 — Driver & Vehicle Service (8082)");
table([
  ["Folder", "services/driver-vehicle-service"],
  ["Main class", "com.ridelink.driver.DriverVehicleServiceApplication"],
  ["Swagger", "http://127.0.0.1:8082/swagger-ui.html"],
  ["Open in IDE", "DriverController.java   DriverProfileService.java   DriverEligibilityFilter.java"],
]);
h3("Commands");
code(
  ".\\mvnw.cmd -pl services/driver-vehicle-service -am spring-boot:run\n" +
  ".\\mvnw.cmd -pl services/driver-vehicle-service -am test"
);
h3("What you own");
bullet("PUT / GET /api/drivers/me  — vehicle, availability, service area, simulated location.");
bullet("GET /api/drivers/eligible?pickup=  — called by Ride Service on assign.");
bullet("Seeded driver1: plate CAB-1234, area Colombo, available true.");
h3("What you say");
say(
  "I own operational driver data, not login. Eligible means available, plate present, and pickup matching the service area. Distance is a dummy string hash, not GPS. Ride Service calls my eligible API. It must not query my tables."
);
h3("Code / JSON you apply in Swagger");
p("First: Member 1 logs in as driver1 on 8081. You Authorize 8082 with that token.");
p("1. GET /api/drivers/me   — expect CAB-1234 and Colombo.");
p("2. Optional PUT /api/drivers/me");
code(
  "{\n" +
  '  "displayName": "Demo Driver",\n' +
  '  "vehicleMake": "Toyota",\n' +
  '  "vehicleModel": "Prius",\n' +
  '  "vehiclePlate": "CAB-1234",\n' +
  '  "vehicleColor": "White",\n' +
  '  "available": true,\n' +
  '  "serviceArea": "Colombo",\n' +
  '  "locationLabel": "Colombo Fort",\n' +
  '  "latitude": 6.93,\n' +
  '  "longitude": 79.85\n' +
  "}"
);
p("3. GET /api/drivers/eligible?pickup=Colombo Fort   — list must include driver1.");
p("4. Negative: set available false, or Ride assign with pickup Jaffna -> 409.");
h3("Likely questions");
qa("Why not inside Account Service?", "Account is identity. This is operations: vehicle, shift, area. Separate data owner.");
qa("How is a driver eligible?", "available == true, vehicle plate present, pickup contains serviceArea or locationLabel (ignore case). Then sort by dummy distance.");
qa("Is location real GPS?", "No. Simulated labels. The brief forbids live maps.");

doc.addPage();
h1("5. Member 3 — Ride Service (8083)");
table([
  ["Folder", "services/ride-service"],
  ["Main class", "com.ridelink.ride.RideServiceApplication"],
  ["Swagger", "http://127.0.0.1:8083/swagger-ui.html"],
  ["Open in IDE", "RideController.java   RideService.java   RideStatus.java   RideServiceTest.java"],
]);
h3("Commands");
code(
  ".\\mvnw.cmd -pl services/ride-service -am spring-boot:run\n" +
  ".\\mvnw.cmd -pl services/ride-service -am test"
);
h3("What you own");
bullet("POST /api/rides  — passenger creates REQUESTED.");
bullet("POST /api/rides/{id}/assign  — REST call to Driver eligible API.");
bullet("accept -> start -> complete (driver). complete calls Fare /api/fares/final.");
bullet("POST /api/rides/{id}/payment  — Fare Service attaches paymentId after pay.");
h3("What you say");
say(
  "I own the ride lifecycle. States are REQUESTED, ASSIGNED, ACCEPTED, IN_PROGRESS, COMPLETED, and CANCELLED. Invalid jumps return 400 INVALID_TRANSITION. On assign I call Driver Service. On complete I call Fare Service. I store UUIDs only — never their databases."
);
h3("Code / JSON you apply in Swagger");
p("Passenger token. POST /api/rides");
code('{ "pickup": "Colombo Fort", "destination": "Kandy" }');
p("Copy id. POST /api/rides/{id}/assign");
code("{ }");
p("Member 1 logs in as driver1. You Authorize 8083 with the driver token, then:");
code("POST /api/rides/{id}/accept\nPOST /api/rides/{id}/start\nPOST /api/rides/{id}/complete");
p("GET /api/rides/{id}  — status COMPLETED, fareId set. After Member 4 pays, paymentId is set.");
p("Negative A — no driver. New ride:");
code('{ "pickup": "Jaffna", "destination": "Kandy" }');
p("Then POST assign  ->  409 NO_DRIVER_AVAILABLE.");
p("Negative B — invalid transition. New Colombo ride (still REQUESTED), then POST complete  ->  400 INVALID_TRANSITION.");
h3("Likely questions");
qa("Draw the state machine.", "REQUESTED -> ASSIGNED -> ACCEPTED -> IN_PROGRESS -> COMPLETED. Any of the first four may go to CANCELLED. COMPLETED and CANCELLED are terminal.");
qa("Why REST to Driver, not a queue?", "Assign must return the chosen driver in the same HTTP response. A queue would be eventually consistent and harder to demo.");
qa("What if Fare is down on complete?", "The ride still becomes COMPLETED and fareNote explains the failure.");

doc.addPage();
h1("6. Member 4 — Fare & Payment Service (8084)");
table([
  ["Folder", "services/fare-payment-service"],
  ["Main class", "com.ridelink.fare.FarePaymentServiceApplication"],
  ["Swagger", "http://127.0.0.1:8084/swagger-ui.html"],
  ["Open in IDE", "FarePaymentController.java   FareCalculator.java   FarePaymentService.java"],
]);
h3("Commands");
code(
  ".\\mvnw.cmd -pl services/fare-payment-service -am spring-boot:run\n" +
  ".\\mvnw.cmd -pl services/fare-payment-service -am test"
);
h3("What you own");
bullet("POST /api/fares/estimate  and  POST /api/fares/final");
bullet("POST /api/payments  — simulated. cardLast4 0000 or simulateFailure true -> FAILED.");
bullet("GET /api/payments/{id}/receipt");
bullet("After a successful pay, this service calls Ride POST /api/rides/{id}/payment");
h3("What you say");
say(
  "Fares use a documented formula: total = 150 + (km times 80) + (minutes times 5) in LKR. Distance is simulated from the pickup and destination strings — no Google Maps. Payments are fake records. A successful pay tells Ride Service the paymentId."
);
h3("Code / JSON you apply in Swagger");
p("Passenger token. POST /api/fares/estimate");
code('{ "pickup": "Colombo Fort", "destination": "Kandy" }');
p("Show total and the formula field. After Member 3 completes the ride, POST /api/payments");
code(
  "{\n" +
  '  "rideId": "<paste ride id>",\n' +
  '  "fareId": "<paste fare id from complete>",\n' +
  '  "amount": 813.30,\n' +
  '  "currency": "LKR",\n' +
  '  "method": "CARD",\n' +
  '  "cardLast4": "4242"\n' +
  "}"
);
p("Use the real amount from estimate/complete (often 813.30 for Colombo Fort -> Kandy). Then GET /api/payments/{id}/receipt.");
p("Fail demo — same body but:");
code('{ "cardLast4": "0000" }');
p("Status FAILED. Ride paymentId is not attached.");
h3("Likely questions");
qa("Write the fare formula.", "total = base + (distanceKm * perKm) + (durationMin * perMin). Defaults 150, 80, 5. Distance hashed from place names into about 2-20 km.");
qa("Why not Stripe?", "The brief requires simulated payments only.");
qa("What if Ride is down when you pay?", "The payment row still saves. Attach can be retried.");

doc.addPage();
h1("7. Integrated happy path (all four watching)");
p("Four Chrome tabs. Pickup always Colombo Fort. Destination Kandy.");
bullet("M1: login passenger1 on 8081. Authorize 8081, 8083, 8084 with that token.");
bullet("M4: POST /api/fares/estimate.");
bullet("M3: POST /api/rides then POST assign.");
bullet("M1: login driver1. Give the new token to M2 and M3.");
bullet("M2: GET /api/drivers/eligible?pickup=Colombo Fort.");
bullet("M3: accept, start, complete.");
bullet("M1: login passenger1 again. Authorize 8084 and 8083.");
bullet("M4: pay 4242. M3: GET ride — paymentId filled.");
bullet("Anyone: 401 without token, or 409 pickup Jaffna, or complete a REQUESTED ride (400).");

h1("8. Group questions (every member)");
qa(
  "Why four services, not a monolith?",
  "The brief requires four independently owned business services. A monolith is simpler (one deploy, one DB) but fails G1. Cost: extra JWT copies, no joins, partial failures. We accept that and use timeouts plus UUID references."
);
qa("Does an API Gateway count as a service?", "No. Gateway, Eureka, or config server must not replace one of the four core business services.");
qa(
  "How do services talk?",
  "Synchronous REST. Ride to Driver eligible; Ride to Fare final; Fare to Ride attach payment. REST fits Swagger/Postman and request-response workflows. Async is documented as a later option."
);
qa("How do you avoid a shared database?", "Four H2 files. A service may store another service's UUID. Nobody runs SQL against another module's tables.");
qa("Show me Git and CI.", "One repo. Feature branches and pull requests. GitHub Actions runs ./mvnw -B verify on push/PR.");
qa("Who wrote this service? Prove it.", "Open YOUR controller and one test. Name your folder under services/. Commits must be your GitHub user.");

h2("If they try to trap you");
qa("Where is the mobile app?", "Out of scope. Marks are on APIs, Swagger, and Postman.");
qa("Can I book a real taxi?", "No. Fictional data, simulated location and payment.");
qa("Did AI write this?", "We used tools under institute policy and can explain every line. Declare AI in the report appendix.");

h1("9. One-page cheat (memorise)");
code(
  "Java 17 + Spring Boot. Four services, four DBs.\n" +
  "JWT from Account. Roles: PASSENGER, DRIVER, ADMIN.\n" +
  "Fare: 150 + km*80 + min*5. Distance = hash of place names.\n" +
  "Assign: REST eligible, first/dummy-nearest. Colombo seeded driver.\n" +
  "States: REQUESTED > ASSIGNED > ACCEPTED > IN_PROGRESS > COMPLETED (+ CANCELLED).\n" +
  "401 no token. 409 no driver. 400 bad transition. 0000 pay fails.\n" +
  "Users: passenger1 / driver1 / admin1     password=password\n" +
  "Chrome  http://127.0.0.1:8081  8082  8083  8084  /swagger-ui.html"
);
p(
  "Print this PDF. Each member bookmarks their section. Practise the happy path once with a timer. If Swagger says connection refused, the Java process is not running — start your service, wait for Started ...Application, then refresh Chrome."
);

footer();
doc.end();
stream.on("finish", () => console.log(out));

"""
RideLink IT3130 viva / demo PDF generator.

Install once:
    pip install fpdf2

From the repository root:
    python docs/generate_viva_pdf.py

Output:
    docs/RideLink_Viva_Presentation_Guide.pdf
"""
from pathlib import Path

from fpdf import FPDF

OUT = Path(__file__).with_name("RideLink_Viva_Presentation_Guide.pdf")
FONT = Path(r"C:\Windows\Fonts\arial.ttf")
FONTB = Path(r"C:\Windows\Fonts\arialbd.ttf")
FONTC = Path(r"C:\Windows\Fonts\cour.ttf")


class Guide(FPDF):
    def header(self):
        if self.page_no() == 1:
            return
        self.set_font("Body", "B", 9)
        self.set_text_color(20, 45, 90)
        self.cell(0, 7, "IT3130  |  RideLink  |  Viva & Demo Guide", align="L")
        self.set_font("Body", "", 8)
        self.set_text_color(110, 110, 110)
        self.cell(0, 7, "Four-member backend presentation", align="R", new_x="LMARGIN", new_y="NEXT")
        self.set_draw_color(20, 45, 90)
        self.line(16, 15, 194, 15)
        self.ln(5)

    def footer(self):
        self.set_y(-13)
        self.set_font("Body", "", 8)
        self.set_text_color(110, 110, 110)
        self.cell(0, 8, f"Page {self.page_no()}  |  github.com/IT24104300/RideLink-AD-", align="C")

    def h1(self, text):
        self.set_font("Body", "B", 15)
        self.set_text_color(20, 45, 90)
        self.multi_cell(0, 8, text)
        self.ln(1)

    def h2(self, text):
        self.ln(1.5)
        self.set_font("Body", "B", 12)
        self.set_text_color(30, 90, 140)
        self.multi_cell(0, 7, text)
        self.ln(0.5)

    def h3(self, text):
        self.ln(1)
        self.set_font("Body", "B", 10.5)
        self.set_text_color(40, 40, 40)
        self.multi_cell(0, 6.2, text)

    def p(self, text):
        self.set_font("Body", "", 10)
        self.set_text_color(30, 30, 30)
        self.multi_cell(0, 5.2, text)
        self.ln(1)

    def bullet(self, text):
        self.set_font("Body", "", 10)
        self.set_text_color(30, 30, 30)
        x = self.get_x()
        self.cell(5, 5.2, chr(149))
        self.multi_cell(0, 5.2, text)
        self.set_x(x)

    def say(self, text):
        self.set_font("Body", "", 10)
        self.set_text_color(20, 50, 90)
        self.multi_cell(0, 5.2, 'SAY: "' + text + '"')
        self.ln(1)

    def qa(self, q, a):
        self.set_font("Body", "B", 10)
        self.set_text_color(120, 40, 40)
        self.multi_cell(0, 5.2, "Q: " + q)
        self.set_font("Body", "", 10)
        self.set_text_color(25, 70, 40)
        self.multi_cell(0, 5.2, "A: " + a)
        self.ln(1.2)

    def code(self, text):
        self.set_fill_color(244, 246, 248)
        self.set_font("Code", "", 8)
        self.set_text_color(20, 20, 20)
        self.multi_cell(0, 4.4, text, fill=True)
        self.ln(1.8)

    def kv_table(self, rows, col1=48):
        self.set_font("Body", "", 9)
        usable = self.w - self.l_margin - self.r_margin
        c2 = usable - col1
        for i, (k, v) in enumerate(rows):
            if i % 2 == 0:
                self.set_fill_color(232, 238, 247)
            else:
                self.set_fill_color(255, 255, 255)
            y = self.get_y()
            x = self.get_x()
            self.set_font("Body", "B", 9)
            self.set_text_color(20, 45, 90)
            self.multi_cell(col1, 6, k, border=1, fill=True)
            h1 = self.get_y() - y
            self.set_xy(x + col1, y)
            self.set_font("Body", "", 9)
            self.set_text_color(30, 30, 30)
            self.multi_cell(c2, 6, v, border=1, fill=True)
            h2 = self.get_y() - y
            self.set_y(y + max(h1, h2))
        self.ln(2)


def build():
    pdf = Guide(format="A4")
    pdf.set_auto_page_break(auto=True, margin=16)
    pdf.set_margins(16, 16, 16)
    pdf.add_font("Body", "", str(FONT))
    pdf.add_font("Body", "B", str(FONTB))
    pdf.add_font("Code", "", str(FONTC) if FONTC.exists() else str(FONT))

    # Cover — same flavour as the official assignment brief
    pdf.add_page()
    pdf.set_fill_color(20, 45, 90)
    pdf.rect(0, 0, 210, 38, "F")
    pdf.set_text_color(255, 255, 255)
    pdf.set_font("Body", "B", 11)
    pdf.set_xy(16, 10)
    pdf.cell(0, 6, "FACULTY OF COMPUTING  |  Department of Information Technology")
    pdf.set_xy(16, 18)
    pdf.set_font("Body", "B", 16)
    pdf.cell(0, 8, "IT3130  Application Development")
    pdf.set_xy(16, 27)
    pdf.set_font("Body", "", 10)
    pdf.cell(0, 6, "Group viva, live Swagger demo, and per-member commands")

    pdf.set_y(48)
    pdf.set_text_color(20, 45, 90)
    pdf.set_font("Body", "B", 28)
    pdf.cell(0, 12, "RideLink", align="C", new_x="LMARGIN", new_y="NEXT")
    pdf.set_font("Body", "B", 13)
    pdf.cell(0, 8, "Viva & Demo Playbook  (four members)", align="C", new_x="LMARGIN", new_y="NEXT")
    pdf.ln(4)
    pdf.set_font("Body", "", 10)
    pdf.set_text_color(40, 40, 40)
    pdf.multi_cell(
        0,
        5.5,
        "Print this PDF. Each member keeps their own section open. "
        "There is no website. Swagger UI is the official client in the brief. "
        "All commands below were verified against this repository.",
        align="C",
    )
    pdf.ln(4)
    pdf.kv_table(
        [
            ("Module", "IT3130 Application Development"),
            ("Assessment", "Group assignment 30%  +  scheduled demonstration / viva"),
            ("Due", "01/10/2026"),
            ("Repo", "https://github.com/IT24104300/RideLink-AD-"),
            ("Stack", "Java 17  |  Spring Boot 3.4  |  Maven Wrapper  |  H2 per service"),
            ("Demo UI", "Chrome  http://127.0.0.1:PORT/swagger-ui.html"),
            ("Users", "passenger1  /  driver1  /  admin1     password = password"),
        ]
    )

    pdf.h3("Who does what on the viva")
    pdf.kv_table(
        [
            ("Member 1", "Account Service   port 8081   login, JWT, /me"),
            ("Member 2", "Driver & Vehicle  port 8082   profile, eligible list"),
            ("Member 3", "Ride Management   port 8083   create, assign, lifecycle (the story)"),
            ("Member 4", "Fare & Payment    port 8084   estimate, pay 4242, receipt"),
        ]
    )
    pdf.set_font("Body", "", 9)
    pdf.set_text_color(90, 90, 90)
    pdf.multi_cell(
        0,
        5,
        "Not leaked exam papers. These are the clicks, commands, and JSON bodies a marker "
        "expects if they grade architecture, REST, JWT, tests, Git, and a live Swagger demo.",
    )

    # Day plan
    pdf.add_page()
    pdf.h1("1. What to do on the viva day")
    pdf.p(
        "It is a group presentation AND an individual viva. Show the running APIs in Chrome Swagger, "
        "then each person is questioned. A taxi website is not required. If they ask 'where is the UI?', "
        "answer: Swagger is the UI the brief asked for."
    )
    pdf.h3("Suggested 12-15 minute flow")
    pdf.bullet("0:00-1:00   All four Java processes already running. One laptop, Chrome, four tabs.")
    pdf.bullet("1:00-2:30   Any member (often Member 3) explains: four services, four DBs, JWT.")
    pdf.bullet("2:30-5:00   Member 1  Account: login, Authorize, GET /me.")
    pdf.bullet("5:00-7:00   Member 2  Driver: GET /me, eligible?pickup=Colombo Fort.")
    pdf.bullet("7:00-10:00  Member 3  Ride: create, assign, accept, start, complete.")
    pdf.bullet("10:00-12:00 Member 4  Fare estimate, pay 4242, receipt.")
    pdf.bullet("12:00-13:30 One negative: 401, 409 Jaffna, 400 REQUESTED-complete, or card 0000.")
    pdf.bullet("13:30+      Individual questions. Everyone must explain the WHOLE system.")
    pdf.h3("Rules while presenting")
    pdf.bullet("Use Chrome or Edge. Do not use Cursor Simple Browser.")
    pdf.bullet("Pickup must include the word Colombo so seeded driver1 is eligible.")
    pdf.bullet("Switching passenger/driver means LOGIN AGAIN and Authorize with the NEW token.")
    pdf.bullet("Swagger Authorize: paste the token only. Do not type the word Bearer.")
    pdf.bullet("Authorize is per browser tab. Repeat it on 8082, 8083 and 8084.")

    pdf.h1("2. Start everything BEFORE they sit down")
    pdf.p("From the repository root in PowerShell. Four terminals. Order: Account, Driver, Fare, then Ride.")
    pdf.code(
        "$env:JAVA_HOME = \"C:\\Program Files\\Microsoft\\jdk-21.0.11.10-hotspot\"\n"
        "cd C:\\Users\\MSI\\Desktop\\Pro\n"
        "\n"
        ".\\mvnw.cmd -pl services/account-service -am spring-boot:run\n"
        ".\\mvnw.cmd -pl services/driver-vehicle-service -am spring-boot:run\n"
        ".\\mvnw.cmd -pl services/fare-payment-service -am spring-boot:run\n"
        ".\\mvnw.cmd -pl services/ride-service -am spring-boot:run"
    )
    pdf.p("Wait until each terminal prints Started ...Application. Then check health (expect {\"status\":\"UP\"}):")
    pdf.code(
        "http://127.0.0.1:8081/actuator/health\n"
        "http://127.0.0.1:8082/actuator/health\n"
        "http://127.0.0.1:8083/actuator/health\n"
        "http://127.0.0.1:8084/actuator/health"
    )
    pdf.h3("IntelliJ IDEA (same for every member)")
    pdf.bullet("File -> Open -> the root pom.xml (whole RideLink project, not one service folder).")
    pdf.bullet("Project SDK = JDK 17 or 21. Do NOT run the parent module.")
    pdf.bullet("Run only YOUR main class:")
    pdf.code(
        "Member 1   com.ridelink.account.AccountServiceApplication\n"
        "Member 2   com.ridelink.driver.DriverVehicleServiceApplication\n"
        "Member 3   com.ridelink.ride.RideServiceApplication\n"
        "Member 4   com.ridelink.fare.FarePaymentServiceApplication"
    )
    pdf.p("Tests before the viva (whole project):")
    pdf.code(".\\mvnw.cmd -q test")
    pdf.h2("Opening lines (any member, 60 seconds)")
    pdf.say(
        "RideLink is a fictional ride-sharing backend. We split it into four Spring Boot "
        "microservices in Java: Account, Driver and Vehicle, Ride, and Fare and Payment. "
        "Each service owns its own H2 database. Account issues a JWT; the others validate "
        "the same secret and enforce passenger, driver, and admin roles. We demo with Swagger, "
        "because the brief is backend-only."
    )

    # Member 1
    pdf.add_page()
    pdf.h1("3. Member 1 — Account Service (8081)")
    pdf.kv_table(
        [
            ("Folder", "services/account-service"),
            ("Main class", "com.ridelink.account.AccountServiceApplication"),
            ("Swagger", "http://127.0.0.1:8081/swagger-ui.html"),
            ("Open in IDE", "AccountController.java   AccountService.java   AccountServiceTest.java"),
        ]
    )
    pdf.h3("Commands")
    pdf.code(
        ".\\mvnw.cmd -pl services/account-service -am spring-boot:run\n"
        ".\\mvnw.cmd -pl services/account-service -am test"
    )
    pdf.h3("What you own")
    pdf.bullet("POST /api/accounts/register  — passenger or driver only (ADMIN self-register is rejected).")
    pdf.bullet("POST /api/accounts/login     — returns accessToken (JWT).")
    pdf.bullet("GET / PUT /api/accounts/me   — profile.")
    pdf.bullet("Admin: GET /api/accounts/{id}   PATCH /api/accounts/{id}/status")
    pdf.h3("What you say (45 seconds)")
    pdf.say(
        "I own Account Service. Passwords are stored as BCrypt hashes, never plaintext. "
        "Login signs a JWT with the account UUID, username, and role. Other services never "
        "open my database; they only read the token."
    )
    pdf.h3("Code / JSON you apply in Swagger")
    pdf.p("1. POST /api/accounts/login")
    pdf.code('{ "username": "passenger1", "password": "password" }')
    pdf.p("Copy accessToken. Click Authorize (green lock). Paste the token. Do not type Bearer.")
    pdf.p("2. GET /api/accounts/me   — must return passenger1.")
    pdf.p("3. Optional register (role PASSENGER or DRIVER only):")
    pdf.code(
        "{\n"
        '  "username": "newpass1",\n'
        '  "password": "password1",\n'
        '  "email": "newpass1@ridelink.local",\n'
        '  "fullName": "New Passenger",\n'
        '  "phone": "0771111111",\n'
        '  "role": "PASSENGER"\n'
        "}"
    )
    pdf.p("4. Negative: clear Authorize, GET /api/accounts/me  ->  401 UNAUTHORIZED.")
    pdf.p("5. Optional admin: login as admin1, then PATCH status:")
    pdf.code('{ "status": "SUSPENDED" }')
    pdf.h3("Likely questions")
    pdf.qa("Why JWT instead of sessions?", "Four separate apps. A session cookie on one server is not trusted by the others. A signed token is validated locally with the shared secret.")
    pdf.qa("What is in the token?", "sub = account UUID, username, role (PASSENGER / DRIVER / ADMIN), expiry. Signed HS256.")
    pdf.qa("Can a passenger register as ADMIN?", "No. Self-registration of ADMIN is rejected. Admin accounts are seeded.")

    # Member 2
    pdf.add_page()
    pdf.h1("4. Member 2 — Driver & Vehicle Service (8082)")
    pdf.kv_table(
        [
            ("Folder", "services/driver-vehicle-service"),
            ("Main class", "com.ridelink.driver.DriverVehicleServiceApplication"),
            ("Swagger", "http://127.0.0.1:8082/swagger-ui.html"),
            ("Open in IDE", "DriverController.java   DriverProfileService.java   DriverEligibilityFilter.java"),
        ]
    )
    pdf.h3("Commands")
    pdf.code(
        ".\\mvnw.cmd -pl services/driver-vehicle-service -am spring-boot:run\n"
        ".\\mvnw.cmd -pl services/driver-vehicle-service -am test"
    )
    pdf.h3("What you own")
    pdf.bullet("PUT / GET /api/drivers/me  — vehicle, availability, service area, simulated location.")
    pdf.bullet("GET /api/drivers/eligible?pickup=  — called by Ride Service on assign.")
    pdf.bullet("Seeded driver1: plate CAB-1234, area Colombo, available true.")
    pdf.h3("What you say")
    pdf.say(
        "I own operational driver data, not login. Eligible means available, plate present, "
        "and pickup matching the service area. Distance is a dummy string hash, not GPS. "
        "Ride Service calls my eligible API. It must not query my tables."
    )
    pdf.h3("Code / JSON you apply in Swagger")
    pdf.p("First: Member 1 logs in as driver1 on 8081. You Authorize 8082 with that token.")
    pdf.p("1. GET /api/drivers/me   — expect CAB-1234 and Colombo.")
    pdf.p("2. Optional PUT /api/drivers/me")
    pdf.code(
        "{\n"
        '  "displayName": "Demo Driver",\n'
        '  "vehicleMake": "Toyota",\n'
        '  "vehicleModel": "Prius",\n'
        '  "vehiclePlate": "CAB-1234",\n'
        '  "vehicleColor": "White",\n'
        '  "available": true,\n'
        '  "serviceArea": "Colombo",\n'
        '  "locationLabel": "Colombo Fort",\n'
        '  "latitude": 6.93,\n'
        '  "longitude": 79.85\n'
        "}"
    )
    pdf.p("3. GET /api/drivers/eligible?pickup=Colombo Fort   — list must include driver1.")
    pdf.p("4. Negative: set available false, or Ride assign with pickup Jaffna -> 409.")
    pdf.h3("Likely questions")
    pdf.qa("Why not inside Account Service?", "Account is identity. This is operations: vehicle, shift, area. Separate data owner.")
    pdf.qa("How is a driver eligible?", "available == true, vehicle plate present, pickup contains serviceArea or locationLabel (ignore case). Then sort by dummy distance.")
    pdf.qa("Is location real GPS?", "No. Simulated labels. The brief forbids live maps.")

    # Member 3
    pdf.add_page()
    pdf.h1("5. Member 3 — Ride Service (8083)")
    pdf.kv_table(
        [
            ("Folder", "services/ride-service"),
            ("Main class", "com.ridelink.ride.RideServiceApplication"),
            ("Swagger", "http://127.0.0.1:8083/swagger-ui.html"),
            ("Open in IDE", "RideController.java   RideService.java   RideStatus.java   RideServiceTest.java"),
        ]
    )
    pdf.h3("Commands")
    pdf.code(
        ".\\mvnw.cmd -pl services/ride-service -am spring-boot:run\n"
        ".\\mvnw.cmd -pl services/ride-service -am test"
    )
    pdf.h3("What you own")
    pdf.bullet("POST /api/rides  — passenger creates REQUESTED.")
    pdf.bullet("POST /api/rides/{id}/assign  — REST call to Driver eligible API.")
    pdf.bullet("accept -> start -> complete (driver). complete calls Fare /api/fares/final.")
    pdf.bullet("POST /api/rides/{id}/payment  — Fare Service attaches paymentId after pay.")
    pdf.h3("What you say")
    pdf.say(
        "I own the ride lifecycle. States are REQUESTED, ASSIGNED, ACCEPTED, IN_PROGRESS, "
        "COMPLETED, and CANCELLED. Invalid jumps return 400 INVALID_TRANSITION. On assign I call "
        "Driver Service. On complete I call Fare Service. I store UUIDs only — never their databases."
    )
    pdf.h3("Code / JSON you apply in Swagger")
    pdf.p("Passenger token. POST /api/rides")
    pdf.code('{ "pickup": "Colombo Fort", "destination": "Kandy" }')
    pdf.p("Copy id. POST /api/rides/{id}/assign")
    pdf.code("{ }")
    pdf.p("Member 1 logs in as driver1. You Authorize 8083 with the driver token, then:")
    pdf.code(
        "POST /api/rides/{id}/accept\n"
        "POST /api/rides/{id}/start\n"
        "POST /api/rides/{id}/complete"
    )
    pdf.p("GET /api/rides/{id}  — status COMPLETED, fareId set. After Member 4 pays, paymentId is set.")
    pdf.p("Negative A — no driver. New ride:")
    pdf.code('{ "pickup": "Jaffna", "destination": "Kandy" }')
    pdf.p("Then POST assign  ->  409 NO_DRIVER_AVAILABLE.")
    pdf.p("Negative B — invalid transition. New Colombo ride (still REQUESTED), then POST complete  ->  400 INVALID_TRANSITION.")
    pdf.h3("Likely questions")
    pdf.qa("Draw the state machine.", "REQUESTED -> ASSIGNED -> ACCEPTED -> IN_PROGRESS -> COMPLETED. Any of the first four may go to CANCELLED. COMPLETED and CANCELLED are terminal.")
    pdf.qa("Why REST to Driver, not a queue?", "Assign must return the chosen driver in the same HTTP response. A queue would be eventually consistent and harder to demo.")
    pdf.qa("What if Fare is down on complete?", "The ride still becomes COMPLETED and fareNote explains the failure.")

    # Member 4
    pdf.add_page()
    pdf.h1("6. Member 4 — Fare & Payment Service (8084)")
    pdf.kv_table(
        [
            ("Folder", "services/fare-payment-service"),
            ("Main class", "com.ridelink.fare.FarePaymentServiceApplication"),
            ("Swagger", "http://127.0.0.1:8084/swagger-ui.html"),
            ("Open in IDE", "FarePaymentController.java   FareCalculator.java   FarePaymentService.java"),
        ]
    )
    pdf.h3("Commands")
    pdf.code(
        ".\\mvnw.cmd -pl services/fare-payment-service -am spring-boot:run\n"
        ".\\mvnw.cmd -pl services/fare-payment-service -am test"
    )
    pdf.h3("What you own")
    pdf.bullet("POST /api/fares/estimate  and  POST /api/fares/final")
    pdf.bullet("POST /api/payments  — simulated. cardLast4 0000 or simulateFailure true -> FAILED.")
    pdf.bullet("GET /api/payments/{id}/receipt")
    pdf.bullet("After a successful pay, this service calls Ride POST /api/rides/{id}/payment")
    pdf.h3("What you say")
    pdf.say(
        "Fares use a documented formula: total = 150 + (km times 80) + (minutes times 5) in LKR. "
        "Distance is simulated from the pickup and destination strings — no Google Maps. "
        "Payments are fake records. A successful pay tells Ride Service the paymentId."
    )
    pdf.h3("Code / JSON you apply in Swagger")
    pdf.p("Passenger token. POST /api/fares/estimate")
    pdf.code('{ "pickup": "Colombo Fort", "destination": "Kandy" }')
    pdf.p("Show total and the formula field. After Member 3 completes the ride, POST /api/payments")
    pdf.code(
        "{\n"
        '  "rideId": "<paste ride id>",\n'
        '  "fareId": "<paste fare id from complete>",\n'
        '  "amount": 813.30,\n'
        '  "currency": "LKR",\n'
        '  "method": "CARD",\n'
        '  "cardLast4": "4242"\n'
        "}"
    )
    pdf.p("Use the real amount from estimate/complete (often 813.30 for Colombo Fort -> Kandy). Then GET /api/payments/{id}/receipt.")
    pdf.p("Fail demo — same body but:")
    pdf.code('{ "cardLast4": "0000" }')
    pdf.p("Status FAILED. Ride paymentId is not attached.")
    pdf.h3("Likely questions")
    pdf.qa("Write the fare formula.", "total = base + (distanceKm * perKm) + (durationMin * perMin). Defaults 150, 80, 5. Distance hashed from place names into about 2-20 km.")
    pdf.qa("Why not Stripe?", "The brief requires simulated payments only.")
    pdf.qa("What if Ride is down when you pay?", "The payment row still saves. Attach can be retried.")

    # Integrated path
    pdf.add_page()
    pdf.h1("7. Integrated happy path (all four watching)")
    pdf.p("Four Chrome tabs. Pickup always Colombo Fort. Destination Kandy.")
    pdf.bullet("M1: login passenger1 on 8081. Authorize 8081, 8083, 8084 with that token.")
    pdf.bullet("M4: POST /api/fares/estimate.")
    pdf.bullet("M3: POST /api/rides then POST assign.")
    pdf.bullet("M1: login driver1. Give the new token to M2 and M3.")
    pdf.bullet("M2: GET /api/drivers/eligible?pickup=Colombo Fort.")
    pdf.bullet("M3: accept, start, complete.")
    pdf.bullet("M1: login passenger1 again. Authorize 8084 and 8083.")
    pdf.bullet("M4: pay 4242. M3: GET ride — paymentId filled.")
    pdf.bullet("Anyone: 401 without token, or 409 pickup Jaffna, or complete a REQUESTED ride (400).")

    pdf.h1("8. Group questions (every member)")
    pdf.qa(
        "Why four services, not a monolith?",
        "The brief requires four independently owned business services. A monolith is simpler "
        "(one deploy, one DB) but fails G1. Cost: extra JWT copies, no joins, partial failures. "
        "We accept that and use timeouts plus UUID references.",
    )
    pdf.qa("Does an API Gateway count as a service?", "No. Gateway, Eureka, or config server must not replace one of the four core business services.")
    pdf.qa(
        "How do services talk?",
        "Synchronous REST. Ride to Driver eligible; Ride to Fare final; Fare to Ride attach payment. "
        "REST fits Swagger/Postman and request-response workflows. Async is documented as a later option.",
    )
    pdf.qa("How do you avoid a shared database?", "Four H2 files. A service may store another service's UUID. Nobody runs SQL against another module's tables.")
    pdf.qa("Show me Git and CI.", "One repo. Feature branches and pull requests. GitHub Actions runs ./mvnw -B verify on push/PR.")
    pdf.qa("Who wrote this service? Prove it.", "Open YOUR controller and one test. Name your folder under services/. Commits must be your GitHub user.")

    pdf.h2("If they try to trap you")
    pdf.qa("Where is the mobile app?", "Out of scope. Marks are on APIs, Swagger, and Postman.")
    pdf.qa("Can I book a real taxi?", "No. Fictional data, simulated location and payment.")
    pdf.qa("Did AI write this?", "We used tools under institute policy and can explain every line. Declare AI in the report appendix.")

    pdf.h1("9. One-page cheat (memorise)")
    pdf.code(
        "Java 17 + Spring Boot. Four services, four DBs.\n"
        "JWT from Account. Roles: PASSENGER, DRIVER, ADMIN.\n"
        "Fare: 150 + km*80 + min*5. Distance = hash of place names.\n"
        "Assign: REST eligible, first/dummy-nearest. Colombo seeded driver.\n"
        "States: REQUESTED > ASSIGNED > ACCEPTED > IN_PROGRESS > COMPLETED (+ CANCELLED).\n"
        "401 no token. 409 no driver. 400 bad transition. 0000 pay fails.\n"
        "Users: passenger1 / driver1 / admin1     password=password\n"
        "Chrome  http://127.0.0.1:8081  8082  8083  8084  /swagger-ui.html"
    )
    pdf.p(
        "Print this PDF. Each member bookmarks their section. Practise the happy path once with a timer. "
        "If Swagger says connection refused, the Java process is not running — start your service, "
        "wait for Started ...Application, then refresh Chrome."
    )

    pdf.output(str(OUT))
    print(OUT)


if __name__ == "__main__":
    build()

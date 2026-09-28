# RideLink PDF demo run — correct vs wrong

Checked against:

- Assignment brief: `docs/assignment/IT3130_AD_Group_Assignment_RideLink_Student_Release.pdf`
- Viva guide: `docs/RideLink_Viva_Presentation_Guide.pdf`

Ran locally on 2026-09-28. All four services are up on 8081–8084. `.\mvnw.cmd -q test` passed.

## Live demo (Swagger / API) — all required workflows

| Result | Check | What the PDF asked | What we got |
| --- | --- | --- | --- |
| CORRECT | Health 8081–8084 | `{"status":"UP"}` | UP on all four ports |
| CORRECT | Swagger UI | `http://127.0.0.1:PORT/swagger-ui.html` | HTTP 200 |
| CORRECT | Login `passenger1` / `driver1` / `admin1` | password `password` | JWT issued, roles PASSENGER / DRIVER / ADMIN |
| CORRECT | GET `/api/accounts/me` | passenger1 profile | username `passenger1` |
| CORRECT | GET `/api/drivers/me` | Colombo / `CAB-1234` | plate `CAB-1234`, area Colombo |
| CORRECT | Fare estimate Colombo Fort → Kandy | formula `150 + km*80 + min*5` | total `813.30`, formula present |
| CORRECT | Create ride | REQUESTED | 201 REQUESTED |
| CORRECT | Assign | REST eligible, seeded driver1 | ASSIGNED to `22222222-…` |
| CORRECT | Accept → start → complete | lifecycle | ACCEPTED, IN_PROGRESS, COMPLETED with `fareId` |
| CORRECT | Pay `cardLast4=4242` | simulated payment + attach `paymentId` | COMPLETED; ride `paymentId` set |
| CORRECT | GET receipt | receipt record | `RL-B836D8E7`, merchant RideLink Platforms Ltd |
| CORRECT | 401 no token | unauthorised access | JSON `code: UNAUTHORIZED` |
| CORRECT | Assign pickup Jaffna | no driver | 409 `NO_DRIVER_AVAILABLE` |
| CORRECT | Complete a REQUESTED ride | invalid transition | 400 `INVALID_TRANSITION` |
| CORRECT | Pay `cardLast4=0000` | failed simulated payment | status FAILED |
| CORRECT | Admin self-register | not allowed | 403 Admin accounts cannot be self-registered |

**Live API: all PDF demo steps are CORRECT after the fixes below.**

## What was wrong and what was changed

| Issue vs PDF | Was | Change |
| --- | --- | --- |
| HTTP Basic / generated default password | Unauthenticated `/me` could return Tomcat HTML 401 instead of the JSON error body | Disabled HTTP Basic and form login; excluded `UserDetailsServiceAutoConfiguration` |
| Fare → Ride `paymentId` attach | Payment saved but ride `paymentId` stayed null (call swallowed / raced the transaction) | Attach after commit; log real Ride errors; send `paymentId` as a string |
| Complete REQUESTED ride | 403 (driver not assigned yet) instead of 400 | Validate lifecycle **before** driver-ownership so the viva negative is `INVALID_TRANSITION` |
| GET `/me` colliding with `/{id}` | Possible UUID path match on `me` | Restrict `/{id}` to a UUID pattern |
| Assignment PDF missing from working tree | `docs/assignment/…pdf` not checked out | Restored from git |

## Still wrong vs the assignment PDF (deliverables, not the running app)

These are not code bugs. They are submission items the brief still expects:

| Item | Status |
| --- | --- |
| Technical report PDF (8–12 pages) | Missing — Courseweb package |
| Git release/tag of the assessed version | Not created yet |
| `OWNERS.md` four different students | All four rows currently say IT24104300 |
| This PC’s `JAVA_HOME` | Set to placeholder `C:\Program Files\Java\<your-jdk-folder>`. Services were started with `C:\Program Files\Microsoft\jdk-21.0.11.10-hotspot` as in the viva PDF |

## How it is running now

```powershell
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.11.10-hotspot"
# already running:
# Account 8081, Driver 8082, Fare 8084, Ride 8083
```

Open in Chrome (not Cursor Simple Browser):

- http://127.0.0.1:8081/swagger-ui.html
- http://127.0.0.1:8082/swagger-ui.html
- http://127.0.0.1:8083/swagger-ui.html
- http://127.0.0.1:8084/swagger-ui.html

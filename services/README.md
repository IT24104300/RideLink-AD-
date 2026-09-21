# Services

Four independently runnable Spring Boot apps. Each owns its own database.

| Module | Port | Owner |
| --- | --- | --- |
| [account-service](account-service) | 8081 | Member 1 |
| [driver-vehicle-service](driver-vehicle-service) | 8082 | Member 2 |
| [ride-service](ride-service) | 8083 | Member 3 |
| [fare-payment-service](fare-payment-service) | 8084 | Member 4 |

From the repository root:

```powershell
.\mvnw.cmd -pl services/account-service -am spring-boot:run
```

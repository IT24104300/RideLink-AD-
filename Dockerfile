# syntax=docker/dockerfile:1
# -------------------------------------------------------------
# RideLink Microservices Multi-stage Dockerfile
# Builds all modules and packages the requested service jar.
# -------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

# Copy POM files for efficient layer caching
COPY pom.xml ./
COPY libs/ridelink-common/pom.xml libs/ridelink-common/
COPY services/account-service/pom.xml services/account-service/
COPY services/driver-vehicle-service/pom.xml services/driver-vehicle-service/
COPY services/ride-service/pom.xml services/ride-service/
COPY services/fare-payment-service/pom.xml services/fare-payment-service/

# Copy source trees
COPY libs libs
COPY services services

# Package all modules (tests run in CI pipeline)
RUN mvn clean package -DskipTests

# -------------------------------------------------------------
# Runtime stage
# -------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine AS runtime
ARG SERVICE_MODULE
WORKDIR /app

# Install wget for container healthchecks
RUN apk add --no-cache wget

# Non-root user for container security
RUN addgroup -S ridelink && adduser -S ridelink -G ridelink
USER ridelink:ridelink

# Copy fat jar from builder
COPY --from=builder /workspace/services/${SERVICE_MODULE}/target/*.jar app.jar

ENV JAVA_OPTS="-Xms64m -Xmx256m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Build stage
FROM gradle:8.5-jdk21-alpine AS builder
WORKDIR /app

# Copy Gradle files
COPY gradle gradle
COPY gradle.properties .
COPY settings.gradle .
COPY build.gradle .

# Copy source
COPY entity-variable-storage-core entity-variable-storage-core
COPY entity-variable-storage-spring-boot-starter entity-variable-storage-spring-boot-starter
COPY examples examples

# Build (skip tests for faster image build)
RUN gradle :examples:spring-boot-example:bootJar -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/examples/spring-boot-example/build/libs/*.jar app.jar

ENV EVS_JDBC_URL=jdbc:postgresql://postgres:5432/evs_db
ENV EVS_USERNAME=postgres
ENV EVS_PASSWORD=postgres

EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]

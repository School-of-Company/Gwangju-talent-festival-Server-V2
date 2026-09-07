---
name: docker
description: Dockerfile and docker-compose authoring guide — multi-stage builds, layer caching, security best practices, and compose service wiring.
---

# Docker Guide

## Dockerfile

### Multi-stage build

```dockerfile
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
RUN ./gradlew dependencies --no-daemon
COPY src src
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Layer caching rules

- `COPY` dependency manifests first, run install, then `COPY` source
- Only invalidate layers that actually changed

### Security

- Use specific digest tags, not `latest`
- Run as non-root: `RUN adduser --disabled-password app && USER app`
- Never `COPY . .` before installing dependencies

## docker-compose.yml

```yaml
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/mydb
    depends_on:
      db:
        condition: service_healthy

  db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: mydb
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      retries: 5
```

## .dockerignore

```
.git
build/
.gradle/
*.log
```

# Multi-stage build for Learning Platform
# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml first to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster build)
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the JAR from builder stage
COPY --from=builder /app/target/orm-final-project-1.0-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8080

# Environment variables with defaults (will be overridden by docker-compose)
ENV DB_URL=jdbc:postgresql://db:5432/learning_platform
ENV DB_USERNAME=postgres
ENV DB_PASSWORD=postgres

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

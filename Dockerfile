# ─── Stage 1: Build Jar with Maven ──────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Package application (skip unit tests for fast build)
RUN mvn clean package -DskipTests

# ─── Stage 2: Lightweight JRE Runtime ────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy built JAR from stage 1
COPY --from=build /app/target/*.jar app.jar

# Expose port (Render automatically sets PORT env var)
EXPOSE 8081

# Launch application
ENTRYPOINT ["java", "-jar", "app.jar"]

# =============================================
# DOCKERFILE - Multi-stage build
# =============================================
# Multi-stage builds keep the final image small.
# Stage 1 (builder): has Maven + JDK - used only to compile and package
# Stage 2 (runtime): has only JRE - the lean final image we ship
#
# Without multi-stage: image would include Maven (~300MB) unnecessarily.
# With multi-stage: final image is just JRE + our JAR (~200MB total).

# ---- STAGE 1: BUILD ----
FROM maven:3.9.5-eclipse-temurin-17 AS builder

# Set working directory inside the container
WORKDIR /app

# Copy pom.xml first and download dependencies
# WHY SEPARATELY? Docker caches layers. If only source code changes (not pom.xml),
# Docker reuses the cached dependency layer → much faster rebuilds!
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Now copy source code and build the JAR
COPY src ./src
RUN mvn clean package -DskipTests -B
# -DskipTests: skip tests during Docker build (run them in CI pipeline instead)
# -B: batch mode (no interactive prompts, cleaner logs)

# ---- STAGE 2: RUNTIME ----
# Eclipse Temurin is the official OpenJDK distribution (previously AdoptOpenJDK)
# We use JRE (runtime only) instead of JDK (development kit) - smaller image
FROM eclipse-temurin:17-jre-alpine

# Alpine Linux is a tiny (~5MB) Linux distro, reduces image size further
WORKDIR /app

# Copy ONLY the built JAR from stage 1 - nothing else!
COPY --from=builder /app/target/*.jar app.jar

# Document which port the app uses (does NOT actually publish it - that's docker-compose's job)
EXPOSE 8080

# The command to run when the container starts
# exec form (JSON array) is preferred over shell form: allows proper signal handling
# so Ctrl+C or docker stop gracefully shuts down the JVM
ENTRYPOINT ["java", "-jar", "app.jar"]

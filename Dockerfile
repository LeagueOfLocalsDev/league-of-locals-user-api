# --- Stage 1: The Builder ---
# This stage uses a full JDK and Gradle to build your application's executable JAR file.
# We use a specific version tag for reproducibility.
FROM gradle:8.5-jdk21-jammy AS build
WORKDIR /app

# Copy the entire project context into the container
COPY . .

# Run the Gradle command to build the JAR file.
# The --no-daemon flag is recommended for CI environments.
RUN gradle bootJar --no-daemon


# --- Stage 2: The Final Image ---
# This stage starts from a clean, lightweight Java Runtime Environment (JRE) image.
# It does NOT contain any build tools or source code, making it smaller and more secure.
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy ONLY the compiled JAR file from the 'build' stage into this final image.
COPY --from=build /app/build/libs/*.jar app.jar

# Tell Docker that the container listens on port 8080 at runtime.
EXPOSE 8080

# The command that will run when the container starts.
ENTRYPOINT ["java", "-jar", "app.jar"]
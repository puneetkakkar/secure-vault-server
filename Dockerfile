FROM eclipse-temurin:17-jdk

WORKDIR /app

# Install curl and Maven
RUN apt-get update && \
    apt-get install -y curl maven && \
    rm -rf /var/lib/apt/lists/*

# Copy pom.xml
COPY pom.xml ./

# Download dependencies
RUN mvn dependency:go-offline

# Copy the source code
COPY src ./src

# Build the application
RUN mvn package -DskipTests

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "target/main-0.0.1-SNAPSHOT.jar"] 
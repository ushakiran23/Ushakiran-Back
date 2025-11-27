# Use Eclipse Temurin JDK 21 (Java 21 same as your system)
FROM eclipse-temurin:21-jdk

# Set working directory
WORKDIR /app

# Copy Maven wrapper & pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Allow mvnw execution
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Build without tests
RUN ./mvnw clean package -DskipTests

# Expose backend port (same as your application.properties)
EXPOSE 8081

# Run backend JAR
CMD ["java", "-jar", "target/backend-0.0.1-SNAPSHOT.jar"]

FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src ./src

# Install Maven
RUN apt-get update && apt-get install -y maven

# Build application
RUN mvn clean package -DskipTests

# Create logs directory
RUN mkdir -p logs

# Expose port
EXPOSE 8080

# Run application
CMD ["java", "-jar", "target/search-service-1.0.0.jar"]
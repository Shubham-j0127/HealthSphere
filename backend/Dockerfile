FROM eclipse-temurin:17-jdk-alpine

# Install Maven
RUN apk add --no-cache maven

WORKDIR /app

# Copy backend project files
COPY backend/pom.xml .
COPY backend/src src

# Build the application
RUN mvn clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/healthsphere-backend-1.0.0.jar"]

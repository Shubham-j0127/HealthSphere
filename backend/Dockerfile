FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy backend files explicitly
COPY backend/mvnw .
COPY backend/.mvn .mvn
COPY backend/pom.xml .
COPY backend/src src

RUN chmod +x mvnw && ./mvnw dependency:go-offline

RUN ./mvnw clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/healthsphere-backend-1.0.0.jar"]

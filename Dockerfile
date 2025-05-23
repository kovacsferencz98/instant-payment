# Build stage
FROM maven:3-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Package stage
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/instant-payment-api-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]

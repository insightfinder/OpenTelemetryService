# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . /app
RUN mvn package -DskipTests

# Run stage
FROM openjdk:21
WORKDIR /app
COPY ./src/main/resources/data.yaml ./src/main/resources/server.yaml /app/
COPY --from=build /app/target/otlpserver-1.0.0-SNAPSHOT-jar-with-dependencies.jar app.jar
EXPOSE 4317
ENTRYPOINT ["java","-jar","app.jar"]
# Render: Language = Docker (Java 런타임 미제공 시)
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY gradlew gradlew.bat ./
COPY gradle gradle/
COPY build.gradle settings.gradle ./
COPY src src/
RUN chmod +x gradlew && ./gradlew bootJar -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/DBmatzip-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

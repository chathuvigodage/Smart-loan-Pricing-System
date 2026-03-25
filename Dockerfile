FROM gradle:8.7-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle clean bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# EXPOSE is optional, but keep it clean
EXPOSE 10000

# Important: no change needed here
ENTRYPOINT ["java", "-jar", "app.jar"]
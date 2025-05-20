FROM gradle:8.5-jdk17 AS build
COPY . /app
WORKDIR /app
RUN gradle clean bootJar

FROM openjdk:17
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
FROM openjdk:17
COPY build/libs/DeptManagement_BackEnd-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
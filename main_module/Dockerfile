FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/main_module-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
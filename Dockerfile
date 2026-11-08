FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY app.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

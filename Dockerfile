# Dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8080

# Production 프로파일로 실행 (JVM 옵션 추가)
ENTRYPOINT ["java", "-XX:-UseContainerSupport", "-jar", "-Dspring.profiles.active=prod", "app.jar"]

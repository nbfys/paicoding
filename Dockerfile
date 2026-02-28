FROM openjdk:8-jdk-slim

LABEL maintainer="Paicoding-AI"
WORKDIR /app
COPY paicoding-web/target/paicoding-web-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]

FROM openjdk:17-jdk-slim

COPY stream-personjob/target/stream-personjob-1.0.0.jar /stream-personjob.jar

EXPOSE 8080

WORKDIR /stream-personjob

CMD ["java", "-jar", "/stream-personjob.jar"]
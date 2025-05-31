FROM openjdk:17-jdk-slim

WORKDIR /stream-personjob

COPY target/stream-personjob-*.jar stream-personjob.jar

EXPOSE 8081

CMD ["java", "-jar", "stream-personjob.jar"]
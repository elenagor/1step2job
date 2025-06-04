FROM eclipse-temurin:17

RUN mkdir /app

COPY stream-personjob/target/*.jar /app/stream-personjob.jar
COPY stream-personjob/config/logback.xml /app
COPY stream-personjob/config/stream-personjob.config /app

EXPOSE 8081
WORKDIR /app

CMD ["java", "-Dlogging.config=./logback.xml", "-jar", "./stream-personjob.jar", "--config=./stream-personjob.config"]
FROM eclipse-temurin:17

RUN mkdir /app

COPY resume-matching/target/*.jar /app/resume-matching.jar
COPY resume-matching/config/logback.xml /app
COPY resume-matching/config/resume-matching.config /app

EXPOSE 8082
WORKDIR /app

CMD ["java", "-Dlogging.config=./logback.xml", "-jar", "./resume-matching.jar", "--config=./resume-matching.config"]
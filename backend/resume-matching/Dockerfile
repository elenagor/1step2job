FROM eclipse-temurin:17

RUN mkdir /app

COPY resume-matching/target/*.jar /app/resume-matching.jar

EXPOSE 8082
WORKDIR /app

CMD ["java", "-jar", "./resume-matching.jar"]
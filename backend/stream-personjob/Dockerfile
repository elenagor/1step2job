FROM eclipse-temurin:17

RUN mkdir /app

COPY stream-personjob/target/*.jar /app/stream-personjob.jar

EXPOSE 8081
WORKDIR /app

CMD ["java", "-jar", "./stream-personjob.jar"]
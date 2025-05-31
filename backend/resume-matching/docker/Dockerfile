FROM openjdk:17-jdk-slim

COPY resume-matching/target/resume-matching-1.0.0.jar /resume-matching.jar

EXPOSE 8080

WORKDIR /resume-matching

CMD ["java", "-jar", "/resume-matching.jar"]
FROM openjdk:17-jdk-slim

WORKDIR /resume-matching

COPY target/resume-matching-*.jar resume-matching.jar

EXPOSE 8082

CMD ["java", "-jar", "resume-matching.jar"]
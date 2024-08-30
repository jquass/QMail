FROM openjdk:17
LABEL authors="jquass"

COPY /target/QMail-1.0-SNAPSHOT.jar /tmp/app.jar
COPY /run/QMailService.yml /tmp/app.yml

WORKDIR /tmp

EXPOSE 8080
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar", "server", "app.yml"]


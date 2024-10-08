FROM openjdk:21
LABEL authors="jquass"

WORKDIR /tmp

COPY /target/QMail-1.0-SNAPSHOT.jar /tmp/app.jar
COPY /run/QMailService.yml /tmp/app.yml

EXPOSE 8080
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar", "server", "app.yml"]

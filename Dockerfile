FROM navikt/java:8
LABEL maintainer="Klan J"

COPY target/pam-ontologi-indexer-*.jar app.jar

#COPY target/classes/kafkatrust.jks .

ENV JAVA_OPTS -Dspring.profiles.active=prod

EXPOSE 8080
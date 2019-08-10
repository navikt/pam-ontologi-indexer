FROM navikt/java:12
COPY target/pam-ontologi-indexer-*.jar /app/app.jar

EXPOSE 9023

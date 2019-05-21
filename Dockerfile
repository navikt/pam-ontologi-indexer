FROM navikt/java:8
COPY target/pam-ontologi-indexer-*.jar /app/app.jar

EXPOSE 9023

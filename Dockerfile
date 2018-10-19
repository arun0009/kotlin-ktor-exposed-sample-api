ARG VERSION=8u151

FROM openjdk:${VERSION}-jdk as BUILD

COPY . /kotlin-ktor-exposed-sample-api
WORKDIR /kotlin-ktor-exposed-sample-api
RUN ./gradlew --no-daemon shadowJar

FROM openjdk:${VERSION}-jre

COPY --from=BUILD /kotlin-ktor-exposed-sample-api/build/libs/kotlin-ktor-exposed-sample-api-all.jar /bin/runner/run.jar
WORKDIR /bin/runner

CMD ["java","-jar","run.jar"]
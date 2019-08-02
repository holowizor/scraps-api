ARG JAVA_VERSION=8u151

FROM openjdk:${JAVA_VERSION}-jdk as BUILD
COPY . /devbuild-src
WORKDIR /devbuild-src
RUN ./gradlew --no-daemon fatJar

FROM openjdk:${JAVA_VERSION}-jre
COPY --from=BUILD /devbuild-src/build/libs/*.jar /devbuild/service.jar
WORKDIR /devbuild
EXPOSE 9091
CMD ["java","-jar","service.jar"]
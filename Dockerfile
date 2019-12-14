ARG JAVA_VERSION=8u151

FROM openjdk:${JAVA_VERSION}-jre
COPY ./build/libs/*.jar /devbuild/service.jar
WORKDIR /devbuild
EXPOSE 9091
CMD ["java","-jar","service.jar"]
FROM gradle:7.4.2-jdk17-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle --no-daemon build

FROM eclipse-temurin:17-alpine

RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/drivebot.jar
ENTRYPOINT ["java", "-jar", "/app/drivebot.jar"]
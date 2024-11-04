# Build stage
FROM gradle:7-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

# Final image stage
FROM openjdk:17
EXPOSE 8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/
USER 1001
ENTRYPOINT ["java", "-jar", "/app/raiderio-ladder-backend-all.jar"]
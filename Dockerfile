# jdk version: 17
FROM openjdk:17-jdk-slim

# gradle build
CMD ["java", "-jar", "app.jar"]

# output file
# - version 값은 build.gradle에 정의한 version 값을 따라감
ARG JAR_FILE=build/libs/*-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# run jar file
CMD ["java", "-jar", "app.jar"]

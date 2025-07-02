FROM amazoncorretto:24-alpine

LABEL maintainer="radiantlogic"

# The GitHub action will result in the jar at the root of the project by this point
COPY ./*.jar /app.jar
RUN mkdir /input
RUN mkdir /output

ENTRYPOINT ["java", "-jar", "/openapi-java-client-codegen.jar"]
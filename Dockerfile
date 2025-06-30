FROM amazoncorretto:24-alpine

LABEL maintainer="radiantlogic"

COPY ./codegen-modules/openapi-java-client-codegen/target/openapi-java-client-codegen-*.jar /openapi-java-client-codegen.jar
RUN mkdir /input
RUN mkdir /output

ENTRYPOINT ["java", "-jar", "/openapi-java-client-codegen.jar"]
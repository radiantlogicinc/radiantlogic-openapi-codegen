FROM amazoncorretto:24-alpine

COPY ./codegen-modules/openapi-java-client-codegen/target/openapi-java-client-codegen-*.jar /openapi-java-client-codegen.jar

ENTRYPOINT ["java", "-jar", "/openapi-java-client-codegen.jar"]
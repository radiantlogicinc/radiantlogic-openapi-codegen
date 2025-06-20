# rest_api_java_client_builder

This project provides a tool that generates a Java CLI client from an OpenAPI spec.

## Requirements

- Java 24
- Maven 3.9.9

### Version Managers

This project fully supports `sdkman` for selecting the correct dependency versions.

## Running Locally

### From the CLI

```bash
mvn clean compile \
  -pl rest-api-java-client-builder -am \
  exec:exec@generate \
  -DprogramArgs="ARGUMENTS GO HERE"
```

To see all possible arguments, run with `-DprogramArgs='-h'`

### From IntelliJ


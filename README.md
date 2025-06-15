# rest_api_java_client_builder

This project provides a tool that generates a Java CLI client from an OpenAPI spec.

## Requirements

- Java 24
- Node 22
- Maven 3.9.9

### Version Managers

This project fully supports both `sdkman` and `nodenv` for selecting the correct dependency versions.

## Running Locally

### NPM Setup

The redocly-validator is an NPM project and needs to be initialized in order to run this project locally. Do so with the following:

```bash
cd rest-api-java-client-builder/redocly-validator
npm install
```

### From the CLI

```bash
mvn clean compile \
  exec:exec@generate \
  -DprogramArgs="ARGUMENTS GO HERE"
```

To see all possible arguments, run with `-DprogramArgs='-h'`

### From IntelliJ


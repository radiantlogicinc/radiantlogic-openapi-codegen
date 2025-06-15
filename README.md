# rest_api_java_client_builder

This project provides a tool that generates a Java CLI client from an OpenAPI spec.

## Requirements

- Java 24
- Node 22
- Maven 3.9.9

### Version Managers

## Running Locally

### NPM Setup



### From the CLI

```bash
mvn clean compile \
  exec:exec@generate \
  -DprogramArgs="ARGUMENTS GO HERE"
```

To see all possible arguments, run with `-DprogramArgs='-h'`
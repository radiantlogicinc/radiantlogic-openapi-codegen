# rest_api_java_client_builder

This project provides a tool that generates a Java CLI client from an OpenAPI spec.

## Running Locally

### From the CLI

```bash
mvn clean compile \
  exec:exec@generate \
  -DprogramArgs="ARGUMENTS GO HERE"
```

To see all possible arguments, run with `-DprogramArgs='-h'`
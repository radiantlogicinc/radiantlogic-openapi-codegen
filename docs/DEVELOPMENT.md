# Development Guide

### Modules

This is a multi-module maven project. The primary modules for the codegen will be found in the `codegen-modules` directory. Modules that exist to use (and therefore test) the generated code will be found in the `usage-modules` directory.

By default, only the `codegen-modules` will be impacted by maven commands. Running commands with the `usage` profile (ie, `mvn -P codegen ...`) will execute commands on the `usage-modules` and running commands with the `all` profile (ie, `mvn -P all ...`) will execute commands on all modules.

### Requirements

- Java 24
- Maven 3.9.9

### Version Managers

This project fully supports `sdkman` for selecting the correct dependency versions.

## Running Locally

### IntelliJ Pre-Requisite

Whether running the full codegen application or individual unit tests, the default IntelliJ settings will not correctly execute the program. The following adjustments will need to be made.

1. The working directory should be set to `./codegen-modules/openapi-java-client-codegen`.
2. The maven `generate-resources` goal must be run before launch. IntelliJ by default does not run the whole maven lifecycle so this must be manually configured. This can be configured from any IntelliJ run configuration with the following steps:
    1. Open the run configuration.
    2. Select `Modify Options` -> `Before Launch Task`.
    3. Add a new task for `Add Maven Goal`.
    4. When configuring the goal, the `Command Line Options` value should be set to `generate-resources`.
    5. Apply the changes.

### Full Codegen From the CLI

This executes the codegen CLI tool from source using the following command:

```bash
mvn clean compile \
  -pl codegen-modules/openapi-java-client-codegen -am \
  exec:exec@generate \
  -DprogramArgs="ARGUMENTS GO HERE"
```

To see all possible arguments, run with `-DprogramArgs='-h'`

### Full Codegen From IntelliJ

Run the main class `com.radiantlogic.openapi.codegen.javaclient.Runner` in the module `codegen-modules/openapi-java-client-codegen`. Make sure to configure the pre-requisite settings described above.

### End-to-End Testing

A robust set of end-to-end tests have been constructed to validate the behavior of the Java client code generation. This is how to use them.

First is the `integration.com.radiantlogic.openapi.codegen.javaclient.CodegenIT` class in `codegen-modules/openapi-java-client-codegen`. This test suite executes the code generation against a wide range of official OpenAPI specs from a variety of companies. It generates, compiles, and installs the maven artifacts from those specs.

Next is the full test suite in `usage-modules/openapi-java-client-usage`. Once all tests from `CodegenIT` complete successfully, all the artifacts will be available in the local `.m2` directory. At that point the test suite in this project can be run to execute a variety of java client operations against a mock server. This validates that the generated code performs as-expected.
# radiantlogic-openapi-codegen

## What Is It

This is an extension to the [openapi-generator](https://github.com/OpenAPITools/openapi-generator) that has been significantly enhanced to handle a wide range of permutations that can occur in OpenAPI specifications. The goal is to be able to produce workable code from nearly all OpenAPI specifications out there in the wild. 

The primary use for the generated code is to support building custom connectors for the RadiantLogic IDDM product. The generated code has been designed for maximum compatibility with this product. At the moment only Java client code is outputted.

### Enhancements to openapi-generator

- Inline enums involved in inheritance hierarchies have been separated into their own classes to avoid bytecode conflicts.
- Discriminated unions will have a discriminator that is the correct type, rather than just setting it to String. This mostly applies to enum discriminators.
- Discriminated unions have a variety of mapping issues resolved so that the mapping and de-serialization works consistently and correctly.
- Non-english/ascii operationIds (ie, chinese characters) are fixed to produce workable method names (yes, a major well-known company actually has this in their official spec).
- Anonymous schemas can have names created by openapi-generator that conflict with existing named schemas. This has been corrected.
- Union types that don't fit a natural java inheritance hierarchy (ie, they include primitives) are dismantled because they won't compile.
- Missing inheritance links have been established so polymorphism works consistently with extended types.

### Limitations

#### Guaranteed Failure: Incomplete Discriminated Unions

An OpenAPI discriminated union requires both the `oneOf` and `discriminator.mapping` properties to be fully constructed in order for the generated code to properly map de-serialization. What this looks like is as follows:

```yaml
UnionSchema:
  oneOf:
    - $ref: "#/components/schemas/SchemaOne"
    - $ref: "#/components/schemas/SchemaTwo"
    - $ref: "#/components/schemas/SchemaThree"
  discriminator:
    propertyName: type
    mapping:
      ONE: "#/components/schemas/SchemaOne"
      TWO: "#/components/schemas/SchemaTwo"
      THREE: "#/components/schemas/SchemaThree"
```

However, there are OpenAPI schemas from reputable companies that do not contain this level of correctness. An example of what has been seen in the wild is this:

```yaml
UnionSchema:
  oneOf:
    - $ref: "#/components/schemas/SchemaOne"
    - $ref: "#/components/schemas/SchemaTwo"
    - $ref: "#/components/schemas/SchemaThree"
  discriminator:
    propertyName: type
```

The primary issue is the lack of a mapping for the discriminator. This means that the code generator cannot properly map the parent type to the child types for de-serialization.

At the time of writing, all generated methods that return a type with this kind of incomplete discriminated union will fail.

#### Other Limitations

- Some type information will be stripped out because OpenAPI & JSON is more permissive than Java is, especially with type unions. There are some scenarios where the generator has been modified to strip out type information in order to produce compile-able code.
- The quality of the generated code is directly correlated to the quality of the OpenAPI specification.
- Only OpenAPI 3 is supported.

## Development

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

Next is the full test suite in `test-modules/openapi-java-client-usage`. Once all tests from `CodegenIT` complete successfully, all the artifacts will be available in the local `.m2` directory. At that point the test suite in this project can be run to execute a variety of java client operations against a mock server. This validates that the generated code performs as-expected.
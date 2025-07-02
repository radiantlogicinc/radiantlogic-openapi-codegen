# radiantlogic-openapi-codegen

## What Is It

This is an extension to the [openapi-generator](https://github.com/OpenAPITools/openapi-generator) that has been significantly enhanced to handle a wide range of permutations that can occur in OpenAPI specifications. The goal is to be able to produce workable code from nearly all OpenAPI specifications out there in the wild. 

The primary use for the generated code is to support building custom connectors for the RadiantLogic IDDM product. The generated code has been designed for maximum compatibility with this product. At the moment only Java client code is outputted.

### Enhancements to openapi-generator

- Inline enums involved in inheritance hierarchies have been separated into their own classes to avoid bytecode conflicts.
- Non-english/ascii operationIds (ie, chinese characters) are fixed to produce workable method names (yes, a major well-known company actually has this in their official spec).
- Anonymous schemas can have names created by openapi-generator that conflict with existing named schemas. This has been corrected.
- Union types that don't fit a natural java inheritance hierarchy (ie, they include primitives) are dismantled because they won't compile.
- Missing inheritance links have been established so polymorphism works consistently with extended types.
- Discriminated unions will have a discriminator that is the correct type, rather than just setting it to String. This mostly applies to enum discriminators.
- Discriminated unions have a variety of mapping issues resolved so that the mapping and de-serialization works consistently and correctly.
- Discriminated unions that are mis-configured and lack a proper discriminator mapping will work via a "Raw" Types feature. See this [link](./docs/RAW_TYPES.md) for details.

### Limitations

- Some type information will be stripped out because OpenAPI & JSON is more permissive than Java is, especially with type unions. There are some scenarios where the generator has been modified to strip out type information in order to produce compile-able code.
- The quality of the generated code is directly correlated to the quality of the OpenAPI specification.
- Only OpenAPI 3 is supported.

## Using the Codegen

TBD

## Additional Documentation

- [Detailed Explanation of "Raw" Types In Generated Code](./docs/RAW_TYPES.md)
- [Development Guide](./docs/DEVELOPMENT.md)
- [GitHub Actions CI/CD](./docs/CICD.md)

NOTE: Pay special attention to the CI/CD guide for release instructions.
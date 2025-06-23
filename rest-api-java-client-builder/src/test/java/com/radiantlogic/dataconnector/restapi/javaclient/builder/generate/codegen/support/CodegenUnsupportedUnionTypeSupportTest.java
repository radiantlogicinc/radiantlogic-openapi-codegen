package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CodegenUnsupportedUnionTypeSupportTest {
  private static final OpenAPI openAPI = new OpenAPI();

  /** Construct the data to use in the tests. */
  @BeforeAll
  static void beforeAll() {
    constructOpenAPISchemas();
  }

  private static void constructOpenAPISchemas() {
    final ObjectSchema objectPropSchema = new ObjectSchema();
    objectPropSchema.setName("objectProp");

    final StringSchema stringPropSchema = new StringSchema();
    stringPropSchema.setName("stringProp");

    final ObjectSchema objectChildSchema = new ObjectSchema();
    objectChildSchema.setName("objectChild");

    final ObjectSchema objectChildSchema2 = new ObjectSchema();
    objectChildSchema2.setName("objectChild2");

    final StringSchema stringChildSchema = new StringSchema();
    stringChildSchema.setName("stringChild");

    final Schema<?> validOneOfSchema = new Schema<>();
    validOneOfSchema.setName("validOneOf");
    validOneOfSchema.setOneOf(List.of(objectChildSchema, objectChildSchema2));

    final Schema<?> invalidOneOfSchema = new Schema<>();
    invalidOneOfSchema.setName("invalidOneOf");
    invalidOneOfSchema.setOneOf(List.of(objectChildSchema, stringChildSchema));

    final Schema<?> validOneOfRefSchema = new Schema<>();
    validOneOfRefSchema.setName("validOneOfRef");
    validOneOfRefSchema.set$ref("#/components/schemas/validOneOf");

    final Schema<?> invalidOneOfRefSchema = new Schema<>();
    invalidOneOfRefSchema.setName("invalidOneOfRef");
    invalidOneOfRefSchema.set$ref("#/components/schemas/invalidOneOf");

    final ObjectSchema allPropertiesValidSchema = new ObjectSchema();
    allPropertiesValidSchema.setName("allPropertiesValid");
    allPropertiesValidSchema.setProperties(
        toSchemaMap(objectPropSchema, stringPropSchema, validOneOfSchema, validOneOfRefSchema));

    final Components components = new Components();
    // openapi-generator using raw types forces me to use one here
    components.setSchemas(
        toSchemaMap(
            objectPropSchema,
            stringPropSchema,
            objectChildSchema,
            stringChildSchema,
            objectChildSchema2,
            validOneOfSchema,
            invalidOneOfSchema,
            validOneOfRefSchema,
            invalidOneOfRefSchema));
    openAPI.setComponents(components);
  }

  private static Map<String, Schema> toSchemaMap(@NonNull final Schema<?>... schemas) {
    Arrays.stream(schemas).collect(Collectors.toMap(Schema::getName, Function.identity()));
  }

  @Test
  void itHasNoUnsupportedUnions() {
    throw new RuntimeException();
  }

  @Test
  void itHasDirectUnsupportedUnionInOneOf() {
    throw new RuntimeException();
  }

  @Test
  void itHasDirectUnsupportedUnionInAnyOf() {
    throw new RuntimeException();
  }

  @Test
  void itHasRefUnsupportedUnionInOneOf() {
    throw new RuntimeException();
  }

  @Test
  void itHasRefUnsupportedUnionInAnyOf() {
    throw new RuntimeException();
  }
}

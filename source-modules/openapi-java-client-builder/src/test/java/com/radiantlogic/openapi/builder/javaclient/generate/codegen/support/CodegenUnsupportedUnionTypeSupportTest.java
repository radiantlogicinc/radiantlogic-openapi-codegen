package com.radiantlogic.openapi.builder.javaclient.generate.codegen.support;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.NonNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenProperty;

public class CodegenUnsupportedUnionTypeSupportTest {
  private static final String SCHEMA_OBJECT_PROP = "objectProp";
  private static final String SCHEMA_STRING_PROP = "stringProp";
  private static final String SCHEMA_OBJECT_CHILD = "objectChild";
  private static final String SCHEMA_OBJECT_CHILD2 = "objectChild2";
  private static final String SCHEMA_STRING_CHILD = "stringChild";
  private static final String SCHEMA_VALID_ONE_OF = "validOneOf";
  private static final String SCHEMA_VALID_ANY_OF = "validAnyOf";
  private static final String SCHEMA_INVALID_ONE_OF = "invalidOneOf";
  private static final String SCHEMA_INVALID_ANY_OF = "invalidAnyOf";
  private static final String SCHEMA_VALID_ONE_OF_REF = "validOneOfRef";
  private static final String SCHEMA_VALID_ANY_OF_REF = "validAnyOfRef";
  private static final String SCHEMA_INVALID_ONE_OF_REF = "invalidOneOfRef";
  private static final String SCHEMA_INVALID_ANY_OF_REF = "invalidAnyOfRef";
  private static final String SCHEMA_ALL_PROPERTIES_VALID = "allPropertiesValid";
  private static final String SCHEMA_HAS_PROPERTY_DIRECT_INVALID_ONE_OF =
      "hasPropertyDirectInvalidOneOf";
  private static final String SCHEMA_HAS_PROPERTY_DIRECT_INVALID_ANY_OF =
      "hasPropertyDirectInvalidAnyOf";
  private static final String SCHEMA_HAS_PROPERTY_REF_INVALID_ONE_OF = "hasPropertyRefInvalidOneOf";
  private static final String SCHEMA_HAS_PROPERTY_REF_INVALID_ANY_OF = "hasPropertyRefInvalidAnyOf";

  private static final OpenAPI openAPI = new OpenAPI();

  /** Construct the data to use in the tests. */
  @BeforeAll
  static void beforeAll() {
    constructOpenAPISchemas();
  }

  private static void constructOpenAPISchemas() {
    final ObjectSchema objectPropSchema = new ObjectSchema();
    objectPropSchema.setName(SCHEMA_OBJECT_PROP);

    final StringSchema stringPropSchema = new StringSchema();
    stringPropSchema.setName(SCHEMA_STRING_PROP);

    final ObjectSchema objectChildSchema = new ObjectSchema();
    objectChildSchema.setName(SCHEMA_OBJECT_CHILD);

    final ObjectSchema objectChildSchema2 = new ObjectSchema();
    objectChildSchema2.setName(SCHEMA_OBJECT_CHILD2);

    final StringSchema stringChildSchema = new StringSchema();
    stringChildSchema.setName(SCHEMA_STRING_CHILD);

    final Schema<?> validOneOfSchema = new Schema<>();
    validOneOfSchema.setName(SCHEMA_VALID_ONE_OF);
    validOneOfSchema.setOneOf(List.of(objectChildSchema, objectChildSchema2));

    final Schema<?> validAnyOfSchema = new Schema<>();
    validAnyOfSchema.setName(SCHEMA_VALID_ANY_OF);
    validAnyOfSchema.setAnyOf(List.of(objectChildSchema, objectChildSchema2));

    final Schema<?> invalidOneOfSchema = new Schema<>();
    invalidOneOfSchema.setName(SCHEMA_INVALID_ONE_OF);
    invalidOneOfSchema.setOneOf(List.of(objectChildSchema, stringChildSchema));

    final Schema<?> invalidAnyOfSchema = new Schema<>();
    invalidAnyOfSchema.setName(SCHEMA_INVALID_ANY_OF);
    invalidAnyOfSchema.setAnyOf(List.of(objectChildSchema, stringChildSchema));

    final Schema<?> validOneOfRefSchema = new Schema<>();
    validOneOfRefSchema.setName(SCHEMA_VALID_ONE_OF_REF);
    validOneOfRefSchema.set$ref("#/components/schemas/%s".formatted(SCHEMA_VALID_ONE_OF));

    final Schema<?> validAnyOfRefSchema = new Schema<>();
    validAnyOfRefSchema.setName(SCHEMA_VALID_ANY_OF_REF);
    validAnyOfRefSchema.set$ref("#/components/schemas/%s".formatted(SCHEMA_VALID_ANY_OF));

    final Schema<?> invalidOneOfRefSchema = new Schema<>();
    invalidOneOfRefSchema.setName(SCHEMA_INVALID_ONE_OF_REF);
    invalidOneOfRefSchema.set$ref("#/components/schemas/%s".formatted(SCHEMA_INVALID_ONE_OF));

    final Schema<?> invalidAnyOfRefSchema = new Schema<>();
    invalidAnyOfRefSchema.setName(SCHEMA_INVALID_ANY_OF_REF);
    invalidAnyOfRefSchema.set$ref("#/components/schemas/%s".formatted(SCHEMA_INVALID_ANY_OF));

    final ObjectSchema allPropertiesValidSchema = new ObjectSchema();
    allPropertiesValidSchema.setName(SCHEMA_ALL_PROPERTIES_VALID);
    allPropertiesValidSchema.setProperties(
        toSchemaMap(
            objectPropSchema,
            stringPropSchema,
            validOneOfSchema,
            validOneOfRefSchema,
            validAnyOfSchema,
            validAnyOfRefSchema));

    final ObjectSchema directInvalidOneOfSchema = new ObjectSchema();
    directInvalidOneOfSchema.setName(SCHEMA_HAS_PROPERTY_DIRECT_INVALID_ONE_OF);
    directInvalidOneOfSchema.setProperties(toSchemaMap(invalidOneOfSchema));

    final ObjectSchema directInvalidAnyOfSchema = new ObjectSchema();
    directInvalidAnyOfSchema.setName(SCHEMA_HAS_PROPERTY_DIRECT_INVALID_ANY_OF);
    directInvalidAnyOfSchema.setProperties(toSchemaMap(invalidAnyOfSchema));

    final ObjectSchema refInvalidOneOfSchema = new ObjectSchema();
    refInvalidOneOfSchema.setName(SCHEMA_HAS_PROPERTY_REF_INVALID_ONE_OF);
    refInvalidOneOfSchema.setProperties(toSchemaMap(invalidOneOfRefSchema));

    final ObjectSchema refInvalidAnyOfSchema = new ObjectSchema();
    refInvalidAnyOfSchema.setName(SCHEMA_HAS_PROPERTY_REF_INVALID_ANY_OF);
    refInvalidAnyOfSchema.setProperties(toSchemaMap(invalidAnyOfRefSchema));

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
            invalidOneOfRefSchema,
            validAnyOfSchema,
            invalidAnyOfSchema,
            invalidAnyOfRefSchema,
            allPropertiesValidSchema,
            directInvalidOneOfSchema,
            directInvalidAnyOfSchema,
            refInvalidOneOfSchema,
            refInvalidAnyOfSchema));
    openAPI.setComponents(components);
  }

  private static Map<String, Schema> toSchemaMap(@NonNull final Schema<?>... schemas) {
    return Arrays.stream(schemas).collect(Collectors.toMap(Schema::getName, Function.identity()));
  }

  private static CodegenProperty createProp(@NonNull final String name, final boolean isComplex) {
    final CodegenProperty codegenProperty = new CodegenProperty();
    codegenProperty.setName(name);
    codegenProperty.setBaseName(name);
    if (isComplex) {
      codegenProperty.setComplexType("complexType");
    }
    return codegenProperty;
  }

  private static CodegenProperty createProp(@NonNull final String name) {
    return createProp(name, true);
  }

  private static CodegenProperty createFixedProp(@NonNull final String name) {
    final CodegenProperty prop = createProp(name, true);
    prop.openApiType = "Object";
    prop.dataType = "Object";
    prop.datatypeWithEnum = "Object";
    prop.baseType = "Object";
    prop.defaultValue = null;
    return prop;
  }

  private final CodegenUnsupportedUnionTypeSupport codegenUnsupportedUnionTypeSupport =
      new CodegenUnsupportedUnionTypeSupport();

  @Test
  void itHasNoUnsupportedUnions() {
    // Creating two copies because the code under test mutates the input and this test is to
    // validate that no changes are performed
    final var codegenPropertyLists =
        IntStream.range(0, 2)
            .boxed()
            .map(
                i ->
                    List.of(
                        createProp(SCHEMA_STRING_PROP, false),
                        createProp(SCHEMA_OBJECT_PROP),
                        createProp(SCHEMA_VALID_ONE_OF),
                        createProp(SCHEMA_VALID_ANY_OF),
                        createProp(SCHEMA_VALID_ONE_OF_REF),
                        createProp(SCHEMA_VALID_ANY_OF_REF)))
            .toList();
    final List<CodegenProperty> expectedProps = codegenPropertyLists.get(0);
    final CodegenModel validModel = new CodegenModel();
    validModel.setName(SCHEMA_ALL_PROPERTIES_VALID);
    validModel.setVars(new ArrayList<>(codegenPropertyLists.get(1)));

    final Schema<?> validSchema =
        openAPI.getComponents().getSchemas().get(SCHEMA_ALL_PROPERTIES_VALID);

    codegenUnsupportedUnionTypeSupport.fixUnsupportedUnionTypes(validModel, validSchema, openAPI);
    assertThat(validModel.getVars()).usingRecursiveComparison().isEqualTo(expectedProps);
  }

  @Test
  void itHasDirectUnsupportedUnionInOneOf() {
    final List<CodegenProperty> initialProps = List.of(createProp(SCHEMA_INVALID_ONE_OF));
    final List<CodegenProperty> expectedProps = List.of(createFixedProp(SCHEMA_INVALID_ONE_OF));

    final CodegenModel model = new CodegenModel();
    model.setName(SCHEMA_HAS_PROPERTY_DIRECT_INVALID_ONE_OF);
    model.setVars(new ArrayList<>(initialProps));

    final Schema<?> schema =
        openAPI.getComponents().getSchemas().get(SCHEMA_HAS_PROPERTY_DIRECT_INVALID_ONE_OF);

    codegenUnsupportedUnionTypeSupport.fixUnsupportedUnionTypes(model, schema, openAPI);
    assertThat(model.getVars()).usingRecursiveComparison().isEqualTo(expectedProps);
  }

  @Test
  void itHasDirectUnsupportedUnionInAnyOf() {
    final List<CodegenProperty> initialProps = List.of(createProp(SCHEMA_INVALID_ANY_OF));
    final List<CodegenProperty> expectedProps = List.of(createFixedProp(SCHEMA_INVALID_ANY_OF));

    final CodegenModel model = new CodegenModel();
    model.setName(SCHEMA_HAS_PROPERTY_DIRECT_INVALID_ANY_OF);
    model.setVars(new ArrayList<>(initialProps));

    final Schema<?> schema =
        openAPI.getComponents().getSchemas().get(SCHEMA_HAS_PROPERTY_DIRECT_INVALID_ANY_OF);

    codegenUnsupportedUnionTypeSupport.fixUnsupportedUnionTypes(model, schema, openAPI);
    assertThat(model.getVars()).usingRecursiveComparison().isEqualTo(expectedProps);
  }

  @Test
  void itHasRefUnsupportedUnionInOneOf() {
    final List<CodegenProperty> initialProps = List.of(createProp(SCHEMA_INVALID_ONE_OF_REF));
    final List<CodegenProperty> expectedProps = List.of(createFixedProp(SCHEMA_INVALID_ONE_OF_REF));

    final CodegenModel model = new CodegenModel();
    model.setName(SCHEMA_HAS_PROPERTY_REF_INVALID_ONE_OF);
    model.setVars(new ArrayList<>(initialProps));

    final Schema<?> schema =
        openAPI.getComponents().getSchemas().get(SCHEMA_HAS_PROPERTY_REF_INVALID_ONE_OF);

    codegenUnsupportedUnionTypeSupport.fixUnsupportedUnionTypes(model, schema, openAPI);
    assertThat(model.getVars()).usingRecursiveComparison().isEqualTo(expectedProps);
  }

  @Test
  void itHasRefUnsupportedUnionInAnyOf() {
    final List<CodegenProperty> initialProps = List.of(createProp(SCHEMA_INVALID_ANY_OF_REF));
    final List<CodegenProperty> expectedProps = List.of(createFixedProp(SCHEMA_INVALID_ANY_OF_REF));

    final CodegenModel model = new CodegenModel();
    model.setName(SCHEMA_HAS_PROPERTY_REF_INVALID_ANY_OF);
    model.setVars(new ArrayList<>(initialProps));

    final Schema<?> schema =
        openAPI.getComponents().getSchemas().get(SCHEMA_HAS_PROPERTY_REF_INVALID_ANY_OF);

    codegenUnsupportedUnionTypeSupport.fixUnsupportedUnionTypes(model, schema, openAPI);
    assertThat(model.getVars()).usingRecursiveComparison().isEqualTo(expectedProps);
  }
}

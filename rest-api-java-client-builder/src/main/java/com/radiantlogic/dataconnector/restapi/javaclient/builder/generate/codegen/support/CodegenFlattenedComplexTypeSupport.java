package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.NonNull;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenProperty;
import org.openapitools.codegen.utils.ModelUtils;

/**
 * The term "flattened" comes from the source code of openapi-generator when the type is configured
 * in this way. In short, a type like this is a union type of non-object types, ie `Long | String |
 * Boolean`. Such a thing is valid in OpenAPI & JSON, but not in Java.
 *
 * <p>This class inspects the properties, identifies incorrectly "flattened" types, and replaces
 * them with `Object` because a more precise union would be extraordinarily difficult to configure
 * the generator to support.
 */
public class CodegenFlattenedComplexTypeSupport {
  private static final Pattern SCHEMA_REF_PATTERN = Pattern.compile("^#/components/schemas/(.*)$");

  public void fixIncorrectlyFlattenedPropertyTypes(
      @NonNull final CodegenModel codegenModel,
      @NonNull final Schema<?> schemaModel,
      @NonNull final OpenAPI openAPI) {
    final List<CodegenProperty> fixedVars =
        codegenModel.getVars().stream()
            .map(
                property -> {
                  if (property.getComplexType() != null) {
                    return fixIncorrectComplexType(property, schemaModel);
                  }
                  return property;
                })
            .toList();
    codegenModel.setVars(new ArrayList<>(fixedVars)); // Must be mutable for downstream code
  }

  private CodegenProperty fixIncorrectComplexType(
      @NonNull final CodegenProperty property,
      @NonNull final Schema<?> schemaModel,
      @NonNull final OpenAPI openAPI) {
    final Schema<?> propertySchema =
        Optional.ofNullable((Map<String, Schema<?>>) schemaModel.getProperties())
            .orElseGet(Map::of)
            .get(property.baseName);
    if (propertySchema == null) {
      return property;
    }

    if (!isIncorrectlyFlattened(propertySchema, openAPI)) {
      return property;
    }

    property.openApiType = "Object";
    property.dataType = "Object";
    property.datatypeWithEnum = "Object";
    property.baseType = "Object";
    property.defaultValue = null;
    return property;
  }

  private boolean isIncorrectlyFlattened(
      @NonNull final Schema<?> schema, @NonNull final OpenAPI openAPI) {
    if (schema.getType() != null && schema.getType().equals("object")) {
      return false;
    }

    if (schema.get$ref() != null) {
      final String schemaName = parseSchemaRef(schema.get$ref());
      final Schema<?> refSchema = ModelUtils.getSchema(openAPI, schemaName);
      return isIncorrectlyFlattened(refSchema);
    }

    if (schema.getOneOf() == null && schema.getAnyOf() == null) {
      return false;
    }

    // TODO clean this up
    final long nonObjectOneOfCount =
        Optional.ofNullable((List<Schema<?>>) schema.getOneOf()).stream()
            .flatMap(List::stream)
            .filter(s -> !(s instanceof ObjectSchema))
            .count();

    final long nonObjectAnyOfCount =
        Optional.ofNullable((List<Schema<?>>) schema.getAnyOf()).stream()
            .flatMap(List::stream)
            .filter(s -> !(s instanceof ObjectSchema))
            .count();

    return nonObjectOneOfCount > 0 || nonObjectAnyOfCount > 0;
  }

  private static String parseSchemaRef(final String ref) {
    final Matcher matcher = SCHEMA_REF_PATTERN.matcher(ref);
    if (!matcher.matches()) {
      throw new IllegalStateException("Invalid schema ref: %s".formatted(ref));
    }
    return matcher.group(1);
  }
}

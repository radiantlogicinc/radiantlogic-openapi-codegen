package com.radiantlogic.openapi.builder.javaclient.generate.codegen.support;

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
 * It is possible in JSON to have a property whose type is a fairly open-ended union, such as `Long
 * | String | Object | Array`. OpenAPI supports this with its own schema design. Java, obviously,
 * does not support such a thing. Generated code with this type of union, depending on the details
 * of how the schema is implemented, can produce compile errors.
 *
 * <p>To fix the issue, this class identifies such unsupported union types and dismantles them,
 * replacing them with a simple Object type. It does reduce precision in the generated code, but
 * it's an acceptable tradeoff at the moment to compensate for this problem. A more sophisticated
 * solution can be built in the future.
 */
public class CodegenUnsupportedUnionTypeSupport {
  private static final Pattern SCHEMA_REF_PATTERN = Pattern.compile("^#/components/schemas/(.*)$");

  public void fixUnsupportedUnionTypes(
      @NonNull final CodegenModel codegenModel,
      @NonNull final Schema<?> schemaModel,
      @NonNull final OpenAPI openAPI) {
    final List<CodegenProperty> fixedVars =
        codegenModel.getVars().stream()
            .map(
                property -> {
                  if (property.getComplexType() != null) {
                    return fixIncorrectComplexType(property, schemaModel, openAPI);
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
        Optional.ofNullable(schemaModel.getProperties()).orElseGet(Map::of).get(property.baseName);
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
      return isIncorrectlyFlattened(refSchema, openAPI);
    }

    if (schema.getOneOf() == null && schema.getAnyOf() == null) {
      return false;
    }

    final long nonObjectOneOfCount = getNonObjectCount(schema.getOneOf());
    final long nonObjectAnyOfCount = getNonObjectCount(schema.getAnyOf());
    return nonObjectOneOfCount > 0 || nonObjectAnyOfCount > 0;
  }

  // I dislike using raw types but openapi-generator uses them liberally which gives me no choice
  // due to compiler restrictions in some cases
  private long getNonObjectCount(final List<Schema> schemas) {
    return Optional.ofNullable(schemas).stream()
        .flatMap(List::stream)
        .filter(s -> !(s instanceof ObjectSchema))
        .count();
  }

  private static String parseSchemaRef(final String ref) {
    final Matcher matcher = SCHEMA_REF_PATTERN.matcher(ref);
    if (!matcher.matches()) {
      throw new IllegalStateException("Invalid schema ref: %s".formatted(ref));
    }
    return matcher.group(1);
  }
}

package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support;

import io.swagger.v3.oas.models.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenProperty;

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
  public void fixIncorrectlyFlattenedPropertyTypes(
      @NonNull final CodegenModel codegenModel, @NonNull final Schema<?> schemaModel) {
    final List<CodegenProperty> fixedVars =
        result.getVars().stream()
            .map(
                property -> {
                  if (property.getComplexType() != null) {
                    return fixIncorrectComplexType(property, model);
                  }
                  return property;
                })
            .toList();
    result.setVars(new ArrayList<>(fixedVars)); // Must be mutable for downstream code
  }
}

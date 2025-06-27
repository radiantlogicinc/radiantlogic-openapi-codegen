package com.radiantlogic.openapi.codegen.javaclient.generate.codegen.support;

import com.radiantlogic.openapi.codegen.javaclient.generate.codegen.utils.CodegenConstants;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenOperation;

/**
 * The "Raw" types are automatically added to discriminated union interfaces if they lack the
 * mapping information to correctly serialize/de-serialize to/from the sub types. This is a
 * workaround to compensate for an incomplete OpenAPI specification.
 *
 * <p>This support class updates models and operations so that any usage of a discriminated union
 * lacking its mapping information is adjusted to use the Raw type instead.
 */
public class CodegenRawTypeUsageSupport {
  public void applyRawTypesToOperationReturnTypes(
      @NonNull final List<CodegenOperation> operations,
      @NonNull final Map<String, CodegenModel> allModelsClassMap) {
    operations.stream()
        .map(
            operation -> {
              final CodegenModel returnType = allModelsClassMap.get(operation.returnBaseType);
              return new OperationWithReturnType(operation, returnType);
            })
        .filter(
            opAndType ->
                opAndType.returnType() != null
                    && opAndType.returnType().discriminator != null
                    && !opAndType.returnType().getHasDiscriminatorWithNonEmptyMapping())
        .forEach(
            opAndType -> {
              final String returnBaseType =
                  "%s.Raw".formatted(opAndType.operation().returnBaseType);
              opAndType.operation().returnBaseType = returnBaseType;
              if (CodegenConstants.LIST_TYPE_PATTERN
                  .matcher(opAndType.operation().returnType)
                  .matches()) {
                opAndType.operation().returnType = "List<%s>".formatted(returnBaseType);
              } else {
                opAndType.operation().returnType = returnBaseType;
              }
            });
  }

  private record OperationWithReturnType(
      @NonNull CodegenOperation operation, CodegenModel returnType) {}
}

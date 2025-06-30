package com.radiantlogic.openapi.codegen.javaclient.generate.codegen.support;

import com.radiantlogic.openapi.codegen.javaclient.generate.codegen.utils.CodegenConstants;
import com.radiantlogic.openapi.codegen.javaclient.generate.codegen.utils.CodegenModelUtils;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
  // TODO oneOf with no discriminator needs to be included here
  public void applyRawTypesToModelProperties(
      @NonNull final Map<String, CodegenModel> modelClassMap) {
    modelClassMap.values().stream()
        .flatMap(model -> model.vars.stream())
        .filter(prop -> Objects.nonNull(prop.complexType))
        .filter(prop -> modelClassMap.containsKey(prop.complexType))
        .filter(prop -> CodegenModelUtils.isInvalidUnionType(modelClassMap.get(prop.complexType)))
        .forEach(
            prop -> {
              final String complexType = "%s.Raw".formatted(prop.complexType);
              prop.datatypeWithEnum =
                  prop.datatypeWithEnum.replaceAll(prop.complexType, complexType);
              prop.complexType = complexType;
              if (prop.items != null) {
                prop.items.complexType = complexType;
                prop.items.datatypeWithEnum = complexType;
              }
            });
  }

  // TODO refactor this further
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
                    && CodegenModelUtils.hasOneOfDiscriminatorWithoutMapping(
                        opAndType.returnType()))
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

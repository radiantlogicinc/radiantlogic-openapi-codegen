package com.radiantlogic.openapi.codegen.javaclient.generate.codegen.support;

import com.radiantlogic.openapi.codegen.javaclient.generate.codegen.utils.CodegenConstants;
import com.radiantlogic.openapi.codegen.javaclient.generate.codegen.utils.CodegenModelUtils;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.NonNull;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.CodegenProperty;

/**
 * The "Raw" types are automatically added to discriminated union interfaces if they lack the
 * mapping information to correctly serialize/de-serialize to/from the sub types. This is a
 * workaround to compensate for an incomplete OpenAPI specification.
 *
 * <p>This support class updates models and operations so that any usage of a discriminated union
 * lacking its mapping information is adjusted to use the Raw type instead.
 */
public class CodegenRawTypeUsageSupport {
  public void applyRawTypesToModelProperties(
      @NonNull final Map<String, CodegenModel> modelClassMap) {
    modelClassMap.values().stream()
        .flatMap(model -> model.vars.stream())
        .filter(prop -> Objects.nonNull(prop.complexType))
        .filter(prop -> modelClassMap.containsKey(prop.complexType))
        .filter(prop -> CodegenModelUtils.isInvalidUnionType(modelClassMap.get(prop.complexType)))
        .forEach(CodegenRawTypeUsageSupport::convertPropertyToRawType);
  }

  private static void convertPropertyToRawType(@NonNull final CodegenProperty prop) {
    final String complexType = "%s.Raw".formatted(prop.complexType);
    prop.datatypeWithEnum = prop.datatypeWithEnum.replaceAll(prop.complexType, complexType);
    prop.complexType = complexType;
    if (prop.items != null) {
      prop.items.complexType = complexType;
      prop.items.datatypeWithEnum = complexType;
    }
  }

  public void applyRawTypesToOperationReturnTypes(
      @NonNull final List<CodegenOperation> operations,
      @NonNull final Map<String, CodegenModel> allModelsClassMap) {
    // Transform return types to Raw types where necessary
    operations.stream()
        .filter(operation -> operation.returnBaseType != null)
        .map(operation -> toOperationWithReturnType(operation, allModelsClassMap))
        .filter(opAndType -> CodegenModelUtils.isInvalidUnionType(opAndType.returnType()))
        .forEach(CodegenRawTypeUsageSupport::convertToRawReturnType);

    // TODO need to get the bodyParams and allParams lists to be adjusted

    // Transform request bodies to Raw types where necessary
    operations.stream()
        .filter(operation -> operation.getHasBodyParam() && operation.bodyParam.baseType != null)
        .map(operation -> toOperationWithBodyParam(operation, allModelsClassMap))
        .filter(opAndType -> CodegenModelUtils.isInvalidUnionType(opAndType.bodyParam()))
        .forEach(CodegenRawTypeUsageSupport::convertToRawBodyParam);
  }

  private static void convertToRawBodyParam(@NonNull final OperationWithBodyParam opAndType) {
    final String bodyParamBaseType = "%s.Raw".formatted(opAndType.operation().bodyParam.baseType);
    opAndType.operation().bodyParam.baseType = bodyParamBaseType;
    if (CodegenConstants.LIST_TYPE_PATTERN
        .matcher(opAndType.operation().bodyParam.dataType)
        .matches()) {
      opAndType.operation().bodyParam.dataType = "List<%s>".formatted(bodyParamBaseType);
    } else {
      opAndType.operation().bodyParam.dataType = bodyParamBaseType;
    }
  }

  private static void convertToRawReturnType(@NonNull final OperationWithReturnType opAndType) {
    final String returnBaseType = "%s.Raw".formatted(opAndType.operation().returnBaseType);
    opAndType.operation().returnBaseType = returnBaseType;
    if (CodegenConstants.LIST_TYPE_PATTERN.matcher(opAndType.operation().returnType).matches()) {
      opAndType.operation().returnType = "List<%s>".formatted(returnBaseType);
    } else {
      opAndType.operation().returnType = returnBaseType;
    }
  }

  private static OperationWithReturnType toOperationWithReturnType(
      @NonNull final CodegenOperation operation,
      @NonNull final Map<String, CodegenModel> allModelsClassMap) {
    final CodegenModel returnType = allModelsClassMap.get(operation.returnBaseType);
    return new OperationWithReturnType(operation, returnType);
  }

  private static OperationWithBodyParam toOperationWithBodyParam(
      @NonNull final CodegenOperation operation,
      @NonNull final Map<String, CodegenModel> allModelsClassMap) {
    final CodegenModel bodyParam = allModelsClassMap.get(operation.bodyParam.baseType);
    return new OperationWithBodyParam(operation, bodyParam);
  }

  private record OperationWithReturnType(
      @NonNull CodegenOperation operation, @NonNull CodegenModel returnType) {}

  private record OperationWithBodyParam(
      @NonNull CodegenOperation operation, @NonNull CodegenModel bodyParam) {}
}

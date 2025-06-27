package com.radiantlogic.openapi.codegen.javaclient.generate.codegen.utils;

import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.model.OperationMap;
import org.openapitools.codegen.model.OperationsMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CodegenOperationUtils {
  @NonNull
  public static List<CodegenOperation> operationsMapToOperationsList(
      @NonNull final OperationsMap operationsMap) {
    return Optional.ofNullable(operationsMap.getOperations())
        .map(OperationMap::getOperation)
        .orElseGet(List::of);
  }
}

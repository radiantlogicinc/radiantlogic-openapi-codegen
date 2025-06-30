package com.radiantlogic.openapi.codegen.javaclient.generate.codegen.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.model.OperationMap;
import org.openapitools.codegen.model.OperationsMap;

public class CodegenOperationUtilsTest {
  @Nested
  class OperationsMapToOperationsList {
    @Test
    void itConvertsOperationsMapToList() {
      // Given
      final CodegenOperation operation1 = new CodegenOperation();
      operation1.operationId = "operation1";

      final CodegenOperation operation2 = new CodegenOperation();
      operation2.operationId = "operation2";

      final List<CodegenOperation> operations = List.of(operation1, operation2);

      final OperationMap operationMap = new OperationMap();
      operationMap.setOperation(operations);

      final OperationsMap operationsMap = new OperationsMap();
      operationsMap.put("operations", operationMap);

      // When
      final List<CodegenOperation> result =
          CodegenOperationUtils.operationsMapToList(operationsMap);

      // Then
      assertThat(result).containsExactlyInAnyOrderElementsOf(operations);
    }

    @Test
    void itReturnsEmptyListWhenOperationsIsNull() {
      // Given
      final OperationsMap operationsMap = new OperationsMap();

      // When
      final List<CodegenOperation> result =
          CodegenOperationUtils.operationsMapToList(operationsMap);

      // Then
      assertThat(result).isEmpty();
    }
  }
}

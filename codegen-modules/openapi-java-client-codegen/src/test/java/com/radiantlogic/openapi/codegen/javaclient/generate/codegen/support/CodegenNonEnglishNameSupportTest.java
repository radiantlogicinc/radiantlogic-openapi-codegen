package com.radiantlogic.openapi.codegen.javaclient.generate.codegen.support;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import org.junit.jupiter.api.Test;

public class CodegenNonEnglishNameSupportTest {
  private final CodegenNonEnglishNameSupport codegenNonEnglishNameSupport =
      new CodegenNonEnglishNameSupport();

  @Test
  void itDoesNothingForValidOperationIds() {
    // Arrange
    final OpenAPI openAPI = new OpenAPI();
    final Paths paths = new Paths();
    final PathItem pathItem = new PathItem();
    final Operation operation = new Operation();
    operation.setOperationId("validOperationId");
    pathItem.setGet(operation);
    paths.addPathItem("/test", pathItem);
    openAPI.setPaths(paths);

    // Act
    codegenNonEnglishNameSupport.fixOperationIds(openAPI);

    // Assert
    assertThat(operation.getOperationId()).isEqualTo("validOperationId");
  }

  @Test
  void itStripsNonEnglishOperationIdCharactersAndLeavesTheRest() {
    // Arrange
    final OpenAPI openAPI = new OpenAPI();
    final Paths paths = new Paths();
    final PathItem pathItem = new PathItem();
    final Operation operation = new Operation();
    operation.setOperationId("测试operationId");
    pathItem.setGet(operation);
    paths.addPathItem("/test", pathItem);
    openAPI.setPaths(paths);

    // Act
    codegenNonEnglishNameSupport.fixOperationIds(openAPI);

    // Assert
    assertThat(operation.getOperationId()).isEqualTo("operationId");
  }

  @Test
  void itNullsOperationIdWithNoNonEnglishCharacters() {
    // Arrange
    final OpenAPI openAPI = new OpenAPI();
    final Paths paths = new Paths();
    final PathItem pathItem = new PathItem();
    final Operation operation = new Operation();
    operation.setOperationId("测试");
    pathItem.setGet(operation);
    paths.addPathItem("/test", pathItem);
    openAPI.setPaths(paths);

    // Act
    codegenNonEnglishNameSupport.fixOperationIds(openAPI);

    // Assert
    assertThat(operation.getOperationId()).isNull();
  }
}

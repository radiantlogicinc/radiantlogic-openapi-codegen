package com.radiantlogic.openapi.codegen.javaclient.generate.codegen.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.radiantlogic.openapi.codegen.javaclient.generate.models.ExtendedCodegenProperty;
import org.junit.jupiter.api.Test;

public class CodegenLiteralPropertyNameSupportTest {
  private final CodegenLiteralPropertyNameSupport codegenLiteralPropertyNameSupport =
      new CodegenLiteralPropertyNameSupport();

  @Test
  void itDoesNothingWithValidName() {
    // Arrange
    final ExtendedCodegenProperty property = new ExtendedCodegenProperty();
    property.name = "validName";

    // Act
    codegenLiteralPropertyNameSupport.fixBadNames(property);

    // Assert
    assertThat(property.name).isEqualTo("validName");
    assertThat(property.getJsonName()).isNull();
  }

  @Test
  void itFixesNumericName() {
    // Arrange
    final ExtendedCodegenProperty property = new ExtendedCodegenProperty();
    property.name = "123";

    // Act
    codegenLiteralPropertyNameSupport.fixBadNames(property);

    // Assert
    assertThat(property.name).isEqualTo("value123");
    assertThat(property.getJsonName()).isEqualTo("123");
  }

  @Test
  void itFixesTrueName() {
    // Arrange
    final ExtendedCodegenProperty property = new ExtendedCodegenProperty();
    property.name = "true";

    // Act
    codegenLiteralPropertyNameSupport.fixBadNames(property);

    // Assert
    assertThat(property.name).isEqualTo("valueTrue");
    assertThat(property.getJsonName()).isEqualTo("true");
  }

  @Test
  void itFixesFalseName() {
    // Arrange
    final ExtendedCodegenProperty property = new ExtendedCodegenProperty();
    property.name = "false";

    // Act
    codegenLiteralPropertyNameSupport.fixBadNames(property);

    // Assert
    assertThat(property.name).isEqualTo("valueFalse");
    assertThat(property.getJsonName()).isEqualTo("false");
  }
}

package com.radiantlogic.openapi.builder.javaclient.generate.codegen.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.radiantlogic.openapi.builder.javaclient.generate.models.ExtendedCodegenProperty;
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
    assertThat(property.jsonName).isNull();
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
    assertThat(property.jsonName).isEqualTo("123");
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
    assertThat(property.jsonName).isEqualTo("true");
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
    assertThat(property.jsonName).isEqualTo("false");
  }
}

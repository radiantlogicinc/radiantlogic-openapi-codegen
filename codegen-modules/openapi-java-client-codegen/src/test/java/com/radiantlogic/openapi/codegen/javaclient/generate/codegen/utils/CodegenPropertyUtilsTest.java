package com.radiantlogic.openapi.codegen.javaclient.generate.codegen.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenProperty;

public class CodegenPropertyUtilsTest {
  @Nested
  class IsEnumProperty {
    @Test
    void itIsEnum() {
      // Arrange
      final CodegenProperty property = new CodegenProperty();
      property.isEnum = true;
      property.isEnumRef = false;
      property.isInnerEnum = false;

      // Act
      final boolean result = CodegenPropertyUtils.isEnumProperty(property);

      // Assert
      assertThat(result).isTrue();
    }

    @Test
    void itIsEnumRef() {
      // Arrange
      final CodegenProperty property = new CodegenProperty();
      property.isEnum = false;
      property.isEnumRef = true;
      property.isInnerEnum = false;

      // Act
      final boolean result = CodegenPropertyUtils.isEnumProperty(property);

      // Assert
      assertThat(result).isTrue();
    }

    @Test
    void itIsInnerEnum() {
      // Arrange
      final CodegenProperty property = new CodegenProperty();
      property.isEnum = false;
      property.isEnumRef = false;
      property.isInnerEnum = true;

      // Act
      final boolean result = CodegenPropertyUtils.isEnumProperty(property);

      // Assert
      assertThat(result).isTrue();
    }

    @Test
    void itIsNotEnum() {
      // Arrange
      final CodegenProperty property = new CodegenProperty();
      property.isEnum = false;
      property.isEnumRef = false;
      property.isInnerEnum = false;

      // Act
      final boolean result = CodegenPropertyUtils.isEnumProperty(property);

      // Assert
      assertThat(result).isFalse();
    }
  }

  @Nested
  class IsSamePropertyInChild {
    @Test
    void itIsSameBasename() {
      // Arrange
      final CodegenProperty parentProperty = new CodegenProperty();
      parentProperty.baseName = "testProperty";

      final CodegenProperty childProperty = new CodegenProperty();
      childProperty.baseName = "testProperty";

      // Act
      final boolean result =
          CodegenPropertyUtils.isSamePropertyInChild(parentProperty, childProperty);

      // Assert
      assertThat(result).isTrue();
    }

    @Test
    void itIsNotSameBasename() {
      // Arrange
      final CodegenProperty parentProperty = new CodegenProperty();
      parentProperty.baseName = "testProperty";

      final CodegenProperty childProperty = new CodegenProperty();
      childProperty.baseName = "differentProperty";

      // Act
      final boolean result =
          CodegenPropertyUtils.isSamePropertyInChild(parentProperty, childProperty);

      // Assert
      assertThat(result).isFalse();
    }
  }

  @Nested
  class IsEnumRefProp {
    @Test
    void itIsEnumAndNotOthers() {
      // Arrange
      final CodegenProperty property = new CodegenProperty();
      property.isEnum = true;
      property.isEnumRef = false;
      property.isInnerEnum = false;

      // Act
      final boolean result = CodegenPropertyUtils.isEnumRefProp(property);

      // Assert
      assertThat(result).isFalse();
    }

    @Test
    void itIsInnerEnumAndNotOthers() {
      // Arrange
      final CodegenProperty property = new CodegenProperty();
      property.isEnum = false;
      property.isEnumRef = false;
      property.isInnerEnum = true;

      // Act
      final boolean result = CodegenPropertyUtils.isEnumRefProp(property);

      // Assert
      assertThat(result).isFalse();
    }

    @Test
    void itIsEnumRefAndNotOthers() {
      // Arrange
      final CodegenProperty property = new CodegenProperty();
      property.isEnum = false;
      property.isEnumRef = true;
      property.isInnerEnum = false;

      // Act
      final boolean result = CodegenPropertyUtils.isEnumRefProp(property);

      // Assert
      assertThat(result).isTrue();
    }
  }
}

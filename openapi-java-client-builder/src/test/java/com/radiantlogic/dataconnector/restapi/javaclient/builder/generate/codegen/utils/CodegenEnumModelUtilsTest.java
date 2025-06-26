package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenProperty;

public class CodegenEnumModelUtilsTest {

  @Nested
  class CreateEnumModelFromEnumProp {
    @Test
    void itCreatesEnumModelFromEnumProp() {
      // Arrange
      final CodegenProperty enumProp = new CodegenProperty();
      enumProp.openApiType = "string";
      enumProp.datatypeWithEnum = "StatusEnum";

      final Map<String, Object> allowableValues = new HashMap<>();
      final List<Object> values = List.of("ACTIVE", "INACTIVE", "PENDING");

      final List<Map<String, Object>> enumVars =
          List.of(
              Map.of(
                  CodegenConstants.NAME_KEY, "ACTIVE",
                  CodegenConstants.VALUE_KEY, "ACTIVE"),
              Map.of(
                  CodegenConstants.NAME_KEY, "INACTIVE",
                  CodegenConstants.VALUE_KEY, "INACTIVE"),
              Map.of(
                  CodegenConstants.NAME_KEY, "PENDING",
                  CodegenConstants.VALUE_KEY, "PENDING"));

      allowableValues.put(CodegenConstants.VALUES_KEY, values);
      allowableValues.put(CodegenConstants.ENUM_VARS_KEY, enumVars);
      enumProp.allowableValues = allowableValues;

      // Act
      final CodegenModel result = CodegenEnumModelUtils.createEnumModelFromEnumProp(enumProp);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.name).isEqualTo("StatusEnum");
      assertThat(result.classname).isEqualTo("StatusEnum");
      assertThat(result.isEnum).isTrue();
      assertThat(result.classFilename).isEqualTo("StatusEnum");
      assertThat(result.dataType).isEqualTo("String");

      final Map<String, Object> resultAllowableValues = result.allowableValues;
      assertThat(resultAllowableValues).isNotNull();

      final List<Object> resultValues =
          (List<Object>) resultAllowableValues.get(CodegenConstants.VALUES_KEY);
      assertThat(resultValues).containsExactlyElementsOf(values);

      final List<Map<String, Object>> resultEnumVars =
          (List<Map<String, Object>>) resultAllowableValues.get(CodegenConstants.ENUM_VARS_KEY);
      assertThat(resultEnumVars).hasSize(3);

      // Verify each enum var has been properly processed
      for (int i = 0; i < resultEnumVars.size(); i++) {
        final Map<String, Object> resultEnumVar = resultEnumVars.get(i);
        final Map<String, Object> originalEnumVar = enumVars.get(i);

        assertThat(resultEnumVar.get(CodegenConstants.NAME_KEY))
            .isEqualTo(originalEnumVar.get(CodegenConstants.NAME_KEY));

        // The value should be quoted
        assertThat(resultEnumVar.get(CodegenConstants.VALUE_KEY))
            .isEqualTo("\"" + originalEnumVar.get(CodegenConstants.VALUE_KEY) + "\"");

        // Verify isString flag is set
        assertThat(resultEnumVar.get(CodegenConstants.IS_STRING_KEY)).isEqualTo(true);
      }
    }

    @Test
    void itCreatesEnumModelFromArrayEnumProp() {
      // Arrange
      final CodegenProperty enumProp = new CodegenProperty();
      enumProp.openApiType = "array";
      enumProp.datatypeWithEnum = "List<ColorEnum>";

      final Map<String, Object> allowableValues = new HashMap<>();
      final List<Object> values = List.of("RED", "GREEN", "BLUE");

      final List<Map<String, Object>> enumVars =
          List.of(
              Map.of(
                  CodegenConstants.NAME_KEY, "RED",
                  CodegenConstants.VALUE_KEY, "RED"),
              Map.of(
                  CodegenConstants.NAME_KEY, "GREEN",
                  CodegenConstants.VALUE_KEY, "GREEN"),
              Map.of(
                  CodegenConstants.NAME_KEY, "BLUE",
                  CodegenConstants.VALUE_KEY, "BLUE"));

      allowableValues.put(CodegenConstants.VALUES_KEY, values);
      allowableValues.put(CodegenConstants.ENUM_VARS_KEY, enumVars);
      enumProp.allowableValues = allowableValues;

      // Act
      final CodegenModel result = CodegenEnumModelUtils.createEnumModelFromEnumProp(enumProp);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.name).isEqualTo("ColorEnum");
      assertThat(result.classname).isEqualTo("ColorEnum");
      assertThat(result.isEnum).isTrue();
      assertThat(result.classFilename).isEqualTo("ColorEnum");
      assertThat(result.dataType).isEqualTo("String");

      final Map<String, Object> resultAllowableValues = result.allowableValues;
      assertThat(resultAllowableValues).isNotNull();

      final List<Object> resultValues =
          (List<Object>) resultAllowableValues.get(CodegenConstants.VALUES_KEY);
      assertThat(resultValues).containsExactlyElementsOf(values);

      final List<Map<String, Object>> resultEnumVars =
          (List<Map<String, Object>>) resultAllowableValues.get(CodegenConstants.ENUM_VARS_KEY);
      assertThat(resultEnumVars).hasSize(3);
    }

    @Test
    void itHandlesAlreadyQuotedValues() {
      // Arrange
      final CodegenProperty enumProp = new CodegenProperty();
      enumProp.openApiType = "string";
      enumProp.datatypeWithEnum = "QuotedEnum";

      final Map<String, Object> allowableValues = new HashMap<>();
      final List<Object> values = List.of("\"ALREADY_QUOTED\"");

      final List<Map<String, Object>> enumVars =
          List.of(
              Map.of(
                  CodegenConstants.NAME_KEY, "ALREADY_QUOTED",
                  CodegenConstants.VALUE_KEY, "\"ALREADY_QUOTED\""));

      allowableValues.put(CodegenConstants.VALUES_KEY, values);
      allowableValues.put(CodegenConstants.ENUM_VARS_KEY, enumVars);
      enumProp.allowableValues = allowableValues;

      // Act
      final CodegenModel result = CodegenEnumModelUtils.createEnumModelFromEnumProp(enumProp);

      // Assert
      assertThat(result).isNotNull();

      final List<Map<String, Object>> resultEnumVars =
          (List<Map<String, Object>>) result.allowableValues.get(CodegenConstants.ENUM_VARS_KEY);
      assertThat(resultEnumVars).hasSize(1);

      // The value should remain as is since it's already quoted
      assertThat(resultEnumVars.get(0).get(CodegenConstants.VALUE_KEY))
          .isEqualTo("\"ALREADY_QUOTED\"");
    }

    @Test
    void itHandlesNonStringValues() {
      // Arrange
      final CodegenProperty enumProp = new CodegenProperty();
      enumProp.openApiType = "integer";
      enumProp.datatypeWithEnum = "NumberEnum";

      final Map<String, Object> allowableValues = new HashMap<>();
      final List<Object> values = List.of(1, 2, 3);

      final List<Map<String, Object>> enumVars =
          List.of(
              Map.of(CodegenConstants.NAME_KEY, "ONE", CodegenConstants.VALUE_KEY, 1),
              Map.of(CodegenConstants.NAME_KEY, "TWO", CodegenConstants.VALUE_KEY, 2),
              Map.of(CodegenConstants.NAME_KEY, "THREE", CodegenConstants.VALUE_KEY, 3));

      allowableValues.put(CodegenConstants.VALUES_KEY, values);
      allowableValues.put(CodegenConstants.ENUM_VARS_KEY, enumVars);
      enumProp.allowableValues = allowableValues;

      // Act
      final CodegenModel result = CodegenEnumModelUtils.createEnumModelFromEnumProp(enumProp);

      // Assert
      assertThat(result).isNotNull();

      final List<Map<String, Object>> resultEnumVars =
          (List<Map<String, Object>>) result.allowableValues.get(CodegenConstants.ENUM_VARS_KEY);
      assertThat(resultEnumVars).hasSize(3);

      // Non-string values should remain as is
      assertThat(resultEnumVars.get(0).get(CodegenConstants.VALUE_KEY)).isEqualTo(1);
      assertThat(resultEnumVars.get(0).get(CodegenConstants.IS_STRING_KEY)).isEqualTo(false);
    }

    @Test
    void itHandlesEmptyAllowableValues() {
      // Arrange
      final CodegenProperty enumProp = new CodegenProperty();
      enumProp.openApiType = "string";
      enumProp.datatypeWithEnum = "EmptyEnum";
      enumProp.allowableValues = null;

      // Act
      final CodegenModel result = CodegenEnumModelUtils.createEnumModelFromEnumProp(enumProp);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.name).isEqualTo("EmptyEnum");

      final Map<String, Object> resultAllowableValues = result.allowableValues;
      assertThat(resultAllowableValues).isNotNull();

      final List<Object> resultValues =
          (List<Object>) resultAllowableValues.get(CodegenConstants.VALUES_KEY);
      assertThat(resultValues).isEmpty();

      final List<Map<String, Object>> resultEnumVars =
          (List<Map<String, Object>>) resultAllowableValues.get(CodegenConstants.ENUM_VARS_KEY);
      assertThat(resultEnumVars).isEmpty();
    }
  }
}

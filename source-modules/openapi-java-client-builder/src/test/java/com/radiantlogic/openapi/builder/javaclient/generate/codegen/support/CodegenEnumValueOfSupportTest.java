package com.radiantlogic.openapi.builder.javaclient.generate.codegen.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class CodegenEnumValueOfSupportTest {
  private final CodegenEnumValueOfSupport codegenEnumValueOfSupport =
      new CodegenEnumValueOfSupport();

  @Test
  void itHasStringDatatype() {
    // Arrange
    final List<Map<String, Object>> enumVars = new ArrayList<>();
    final Map<String, Object> enumVar1 = new HashMap<>();
    final Map<String, Object> enumVar2 = new HashMap<>();
    enumVars.add(enumVar1);
    enumVars.add(enumVar2);
    final String dataType = "String";

    // Act
    final List<Map<String, Object>> result =
        codegenEnumValueOfSupport.fixValueOfInEnumVars(enumVars, dataType);

    // Assert
    assertThat(result).hasSize(2);
    assertThat(result.get(0)).containsEntry("useValueOf", true);
    assertThat(result.get(1)).containsEntry("useValueOf", true);
  }

  @Test
  void itHasBigDecimalDatatype() {
    // Arrange
    final List<Map<String, Object>> enumVars = new ArrayList<>();
    final Map<String, Object> enumVar1 = new HashMap<>();
    final Map<String, Object> enumVar2 = new HashMap<>();
    enumVars.add(enumVar1);
    enumVars.add(enumVar2);
    final String dataType = "BigDecimal";

    // Act
    final List<Map<String, Object>> result =
        codegenEnumValueOfSupport.fixValueOfInEnumVars(enumVars, dataType);

    // Assert
    assertThat(result).hasSize(2);
    assertThat(result.get(0)).containsEntry("useValueOf", false);
    assertThat(result.get(1)).containsEntry("useValueOf", false);
  }
}

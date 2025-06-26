package com.radiantlogic.openapi.builder.javaclient.generate.codegen.support;

import java.util.List;
import java.util.Map;
import lombok.NonNull;

/**
 * Enums with numeric datatypes can result in the codegen assigning a value type of BigDecimal,
 * however the model mustache templates will always try to use .valueOf() on the datatype in a way
 * that is invalid for BigDecimal. This sets a property that is added to the custom mustache
 * template in this codegen so that for BigDecimals this is prevented.
 */
public class CodegenEnumValueOfSupport {
  public List<Map<String, Object>> fixValueOfInEnumVars(
      @NonNull final List<Map<String, Object>> enumVars, @NonNull final String dataType) {
    final boolean useValueOf = !dataType.equals("BigDecimal");

    return enumVars.stream().peek(map -> map.put("useValueOf", useValueOf)).toList();
  }
}

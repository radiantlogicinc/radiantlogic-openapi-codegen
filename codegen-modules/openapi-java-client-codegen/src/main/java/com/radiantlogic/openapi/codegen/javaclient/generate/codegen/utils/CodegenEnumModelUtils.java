package com.radiantlogic.openapi.codegen.javaclient.generate.codegen.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import lombok.NonNull;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenProperty;

public class CodegenEnumModelUtils {

  @NonNull
  public static CodegenModel createEnumModelFromEnumProp(@NonNull final CodegenProperty enumProp) {
    final CodegenModel enumModel = new CodegenModel();

    final String typeName = getTypeName(enumProp);
    final List<Object> propAllowableValues = getAllowableValues(enumProp);
    final List<Map<String, Object>> propAllowableEnumVars = getAllowableEnumVars(enumProp);

    final List<Map<String, Object>> enumVars =
        propAllowableEnumVars.stream().map(CodegenEnumModelUtils::createActualEnumVar).toList();

    final Map<String, Object> allowableValues =
        Map.ofEntries(
            Map.entry(CodegenConstants.VALUES_KEY, propAllowableValues),
            Map.entry(CodegenConstants.ENUM_VARS_KEY, enumVars));

    enumModel.name = typeName;
    enumModel.classname = typeName;
    enumModel.isEnum = true;
    enumModel.allowableValues = allowableValues;
    enumModel.classFilename = typeName;
    enumModel.dataType = "String";
    return enumModel;
  }

  @NonNull
  private static String getTypeName(@NonNull final CodegenProperty enumProp) {
    if (!enumProp.openApiType.equals("array")) {
      return enumProp.datatypeWithEnum;
    }

    final Matcher matcher = CodegenConstants.LIST_TYPE_PATTERN.matcher(enumProp.datatypeWithEnum);
    if (!matcher.matches()) {
      throw new IllegalStateException(
          "Array enum property has a name that doesn't match pattern: %s"
              .formatted(enumProp.datatypeWithEnum));
    }
    return matcher.group(1);
  }

  @NonNull
  private static List<Object> getAllowableValues(@NonNull final CodegenProperty enumProp) {
    return Optional.ofNullable(enumProp.allowableValues)
        .map(map -> (List<Object>) map.get(CodegenConstants.VALUES_KEY))
        .orElseGet(List::of);
  }

  private static List<Map<String, Object>> getAllowableEnumVars(
      @NonNull final CodegenProperty enumProp) {
    return Optional.ofNullable(enumProp.allowableValues)
        .map(map -> (List<Map<String, Object>>) map.get(CodegenConstants.ENUM_VARS_KEY))
        .orElseGet(List::of);
  }

  @NonNull
  private static Map<String, Object> createActualEnumVar(
      @NonNull final Map<String, Object> enumVar) {
    final Object value = enumVar.get(CodegenConstants.VALUE_KEY);
    Object newValue;
    if (value instanceof String stringValue
        && !CodegenConstants.QUOTED_STRING_PATTERN.matcher(stringValue).matches()) {
      newValue = "\"%s\"".formatted(stringValue);
    } else {
      newValue = value;
    }

    final Map<String, Object> newMap =
        Map.ofEntries(
            Map.entry(CodegenConstants.NAME_KEY, enumVar.get(CodegenConstants.NAME_KEY)),
            Map.entry(CodegenConstants.VALUE_KEY, newValue),
            Map.entry(CodegenConstants.IS_STRING_KEY, value instanceof String));
    // Needs to be mutable for downstream
    return new HashMap<>(newMap);
  }
}

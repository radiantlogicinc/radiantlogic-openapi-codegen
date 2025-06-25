package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.NonNull;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenProperty;

public class CodegenEnumModelUtils {
  private static final Pattern LIST_TYPE_PATTERN = Pattern.compile("^List<(.*)>$");
  private static final Pattern QUOTED_STRING_PATTERN = Pattern.compile("^\"(.*)\"$");

  @NonNull
  public static CodegenModel createEnumModelFromEnumProp(@NonNull final CodegenProperty enumProp) {
    final CodegenModel enumModel = new CodegenModel();

    // TODO cleanup
    final String typeName;
    if (enumProp.openApiType.equals("array")) {
      final Matcher matcher = LIST_TYPE_PATTERN.matcher(enumProp.datatypeWithEnum);
      if (!matcher.matches()) {
        throw new IllegalStateException(
            "Array enum property has a name that doesn't match pattern: %s"
                .formatted(enumProp.datatypeWithEnum));
      }
      typeName = matcher.group(1);
    } else {
      typeName = enumProp.datatypeWithEnum;
    }

    // TODO cleanup
    final List<Object> propAllowableValuesValues =
        Optional.ofNullable(enumProp.allowableValues)
            .map(map -> (List<Object>) map.get(CodegenConstants.VALUES_KEY))
            .orElseGet(List::of);
    final List<Map<String, Object>> propAllowableValuesEnumVars =
        Optional.ofNullable(enumProp.allowableValues)
            .map(map -> (List<Map<String, Object>>) map.get(CodegenConstants.ENUM_VARS_KEY))
            .orElseGet(List::of);

    // TODO cleanup
    final List<Map<String, Object>> enumVars =
        propAllowableValuesEnumVars.stream()
            .map(
                map -> {
                  final Object value = map.get(CodegenConstants.VALUE_KEY);
                  final Map<String, Object> newMap = new HashMap<>();
                  newMap.put(CodegenConstants.NAME_KEY, map.get(CodegenConstants.NAME_KEY));
                  if (value instanceof String stringValue
                      && !QUOTED_STRING_PATTERN.matcher(stringValue).matches()) {
                    newMap.put(CodegenConstants.VALUE_KEY, "\"%s\"".formatted(stringValue));
                  } else {
                    newMap.put(CodegenConstants.VALUE_KEY, value);
                  }
                  newMap.put(CodegenConstants.IS_STRING_KEY, value instanceof String);
                  return newMap;
                })
            .toList();

    // TODO cleanup
    final Map<String, Object> allowableValues =
        Map.of(
            CodegenConstants.VALUES_KEY,
            propAllowableValuesValues,
            CodegenConstants.ENUM_VARS_KEY,
            enumVars);

    enumModel.name = typeName;
    enumModel.classname = typeName;
    enumModel.isEnum = true;
    enumModel.allowableValues = allowableValues;
    enumModel.classFilename = typeName;
    enumModel.dataType = "String";
    return enumModel;
  }
}

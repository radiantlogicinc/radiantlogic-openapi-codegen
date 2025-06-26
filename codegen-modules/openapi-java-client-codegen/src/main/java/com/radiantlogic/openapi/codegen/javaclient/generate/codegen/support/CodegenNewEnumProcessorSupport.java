package com.radiantlogic.openapi.codegen.javaclient.generate.codegen.support;

import com.radiantlogic.openapi.codegen.javaclient.generate.codegen.utils.CodegenConstants;
import com.radiantlogic.openapi.codegen.javaclient.generate.codegen.utils.CodegenModelUtils;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.model.ModelsMap;
import org.openapitools.codegen.utils.ModelUtils;

/**
 * Other support classes extract a lot of enums from models. These are inline enums that should
 * actually be separate models because inline enums produce more potential compile errors than any
 * other type. This class processes all the new enums, merges duplicates together (preserving all
 * enum values in the process), merges new enums with matching existing ones, etc. In the end it
 * merges the new enums into the existing ModelMaps map so that the codegen can correctly generate
 * the necessary model classes.
 */
public class CodegenNewEnumProcessorSupport {
  public void processNewEnumsAndMergeToModelMaps(
      @NonNull final List<CodegenModel> newEnums,
      @NonNull final Map<String, ModelsMap> allModelMaps,
      @NonNull final String modelPackage,
      @NonNull final Map<String, String> importMapping) {
    final ModelsMap rawEnumModelBase =
        allModelMaps.get(allModelMaps.keySet().stream().findFirst().orElseThrow());

    final Map<String, CodegenModel> allNewEnums =
        newEnums.stream()
            .map(newEnum -> mergeWithExistingEnum(newEnum, allModelMaps))
            .collect(
                Collectors.toMap(
                    CodegenModel::getName,
                    Function.identity(),
                    CodegenNewEnumProcessorSupport::mergeEnumCodegenModels));

    final List<Map<String, String>> importsForEnums = getImportsForEnum(importMapping);

    final ModelsMap enumModelBase = new ModelsMap();
    enumModelBase.putAll(rawEnumModelBase);
    enumModelBase.setImports(importsForEnums);

    allNewEnums.entrySet().stream()
        .map(
            entry -> {
              final ModelsMap modelsMap =
                  CodegenModelUtils.wrapInModelsMap(enumModelBase, modelPackage, entry.getValue());
              return Map.entry(entry.getKey(), modelsMap);
            })
        .forEach(entry -> allModelMaps.put(entry.getKey(), entry.getValue()));
  }

  @NonNull
  private static List<Map<String, String>> getImportsForEnum(
      @NonNull final Map<String, String> importMapping) {
    return importMapping.values().stream()
        .filter(
            importValue ->
                !importValue.startsWith("org.joda")
                    && !importValue.startsWith("com.google")
                    && !importValue.startsWith("com.radiantlogic")
                    && !importValue.startsWith("io.swagger.annotations"))
        .map(importValue -> Map.of(CodegenConstants.IMPORT_KEY, importValue))
        .toList();
  }

  @NonNull
  private static CodegenModel mergeWithExistingEnum(
      @NonNull final CodegenModel newEnum, @NonNull final Map<String, ModelsMap> allModelMaps) {
    final ModelsMap modelsMap = allModelMaps.get(newEnum.name);
    if (modelsMap == null) {
      return newEnum;
    }

    final CodegenModel existingEnumModel = ModelUtils.getModelByName(newEnum.name, allModelMaps);
    return mergeEnumCodegenModels(existingEnumModel, newEnum);
  }

  private static CodegenModel mergeEnumCodegenModels(
      @NonNull final CodegenModel one, @NonNull final CodegenModel two) {
    if (!one.isEnum || !two.isEnum) {
      throw new IllegalArgumentException("Cannot merge non-enum models");
    }
    final var oneEnumVars = getEnumVars(one);
    final var twoEnumVars = getEnumVars(two);
    final Collection<Map<String, Object>> enumVars = mergeEnumVars(oneEnumVars, twoEnumVars);
    final var oneValues = getEnumValues(one);
    final var twoValues = getEnumValues(two);
    final List<Object> values = mergeEnumValues(oneValues, twoValues);

    one.allowableValues =
        Map.of(CodegenConstants.ENUM_VARS_KEY, enumVars, CodegenConstants.VALUES_KEY, values);
    return one;
  }

  @NonNull
  @SuppressWarnings("unchecked")
  private static Collection<Map<String, Object>> getEnumVars(@NonNull final CodegenModel model) {
    return Optional.ofNullable(
            (Collection<Map<String, Object>>)
                model.allowableValues.get(CodegenConstants.ENUM_VARS_KEY))
        .orElseGet(List::of);
  }

  @NonNull
  @SuppressWarnings("unchecked")
  private static List<Object> getEnumValues(@NonNull final CodegenModel model) {
    return Optional.ofNullable(
            (List<Object>) model.allowableValues.get(CodegenConstants.VALUES_KEY))
        .orElseGet(List::of);
  }

  @NonNull
  private static List<Object> mergeEnumValues(
      @NonNull final List<Object> values1, @NonNull final List<Object> values2) {
    return Stream.of(values1.stream(), values2.stream())
        .flatMap(Function.identity())
        .distinct()
        .toList();
  }

  @NonNull
  private static Collection<Map<String, Object>> mergeEnumVars(
      @NonNull final Collection<Map<String, Object>> enumVars1,
      @NonNull final Collection<Map<String, Object>> enumVars2) {
    return Stream.of(enumVars1.stream(), enumVars2.stream())
        .flatMap(Function.identity())
        .collect(
            Collectors.toMap(
                map -> map.get(CodegenConstants.NAME_KEY), Function.identity(), (a, b) -> b))
        .values();
  }
}

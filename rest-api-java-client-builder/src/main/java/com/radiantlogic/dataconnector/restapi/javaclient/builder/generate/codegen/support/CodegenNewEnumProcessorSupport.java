package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support;

import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.utils.CodegenConstants;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.utils.CodegenModelUtils;
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
            .map(
                newEnum -> {
                  return Optional.ofNullable(allModelMaps.get(newEnum.name))
                      .map(
                          e ->
                              mergeEnumCodegenModels(
                                  ModelUtils.getModelByName(newEnum.name, allModelMaps), newEnum))
                      .orElse(newEnum);
                })
            .collect(
                Collectors.toMap(
                    CodegenModel::getName,
                    Function.identity(),
                    CodegenNewEnumProcessorSupport::mergeEnumCodegenModels));

    final List<Map<String, String>> importsForEnums = getImportsForEnum(importMapping);

    final ModelsMap enumModelBase = new ModelsMap();
    enumModelBase.putAll(rawEnumModelBase);
    enumModelBase.setImports(importsForEnums);

    allNewEnums.forEach(
        (key, model) -> {
          allModelMaps.put(
              key, CodegenModelUtils.wrapInModelsMap(enumModelBase, modelPackage, model));
        });
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
        .map(importValue -> Map.of("import", importValue))
        .toList();
  }

  // TODO clean this up
  private static CodegenModel mergeEnumCodegenModels(
      @NonNull final CodegenModel one, @NonNull final CodegenModel two) {
    if (!one.isEnum || !two.isEnum) {
      throw new IllegalArgumentException("Cannot merge non-enum models");
    }
    final var oneEnumVars =
        (Collection<Map<String, Object>>)
            Optional.ofNullable(one.allowableValues.get(CodegenConstants.ENUM_VARS_KEY))
                .orElseGet(List::of);
    final var twoEnumVars =
        (Collection<Map<String, Object>>)
            Optional.ofNullable(two.allowableValues.get(CodegenConstants.ENUM_VARS_KEY))
                .orElseGet(List::of);
    final Collection<Map<String, Object>> enumVars =
        Stream.of(oneEnumVars.stream(), twoEnumVars.stream())
            .flatMap(Function.identity())
            .collect(
                Collectors.toMap(
                    map -> map.get(CodegenConstants.NAME_KEY), Function.identity(), (a, b) -> b))
            .values();
    final var oneValues =
        (List<Object>)
            Optional.ofNullable(one.allowableValues.get(CodegenConstants.VALUES_KEY))
                .orElseGet(List::of);
    final var twoValues =
        (List<Object>)
            Optional.ofNullable(two.allowableValues.get(CodegenConstants.VALUES_KEY))
                .orElseGet(List::of);
    final List<Object> values =
        Stream.of(oneValues.stream(), twoValues.stream())
            .flatMap(Function.identity())
            .distinct()
            .toList();

    one.allowableValues =
        Map.of(CodegenConstants.ENUM_VARS_KEY, enumVars, CodegenConstants.VALUES_KEY, values);
    return one;
  }
}

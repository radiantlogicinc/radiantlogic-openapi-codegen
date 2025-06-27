package com.radiantlogic.openapi.codegen.javaclient.generate.codegen.utils;

import com.radiantlogic.openapi.codegen.javaclient.exceptions.ModelNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.ModelsMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CodegenModelUtils {
  public static boolean hasDiscriminatorChildren(@NonNull final CodegenModel model) {
    return model.discriminator != null && model.discriminator.getMappedModels() != null;
  }

  @NonNull
  public static CodegenModel extractModel(@NonNull final ModelsMap modelsMap) {
    return Optional.ofNullable(modelsMap.getModels())
        .filter(list -> !list.isEmpty())
        .map(List::getFirst)
        .map(ModelMap::getModel)
        .orElseThrow(
            () ->
                new ModelNotFoundException(
                    "ModelsMap had either no models or more than one model, cannot extract CodegenModel"));
  }

  @NonNull
  public static ModelsMap wrapInModelsMap(
      @NonNull final ModelsMap baseModelsMap,
      @NonNull final String modelPackage,
      @NonNull final CodegenModel model) {
    final ModelsMap modelsMap = new ModelsMap();
    modelsMap.putAll(baseModelsMap);

    final String importPath = "%s.%s".formatted(modelPackage, model.classname);

    final ModelMap modelMap = new ModelMap();
    modelMap.setModel(model);
    modelMap.put(CodegenConstants.IMPORT_PATH_KEY, importPath);
    modelsMap.setModels(List.of(modelMap));
    return modelsMap;
  }

  public static boolean hasNonDiscriminatorChildren(@NonNull final CodegenModel model) {
    final boolean hasOneOfChildren = model.oneOf != null && !model.oneOf.isEmpty();
    final boolean hasNoDiscriminatorChildren =
        model.discriminator == null
            || (model.discriminator.getMappedModels() == null
                || model.discriminator.getMappedModels().isEmpty());
    return hasOneOfChildren && hasNoDiscriminatorChildren;
  }

  public static boolean hasOneOfDiscriminatorWithoutMapping(@NonNull final CodegenModel model) {
    final boolean hasOneOf = model.oneOf != null && !model.oneOf.isEmpty();
    final boolean hasDiscriminatorNoMapping =
        model.discriminator != null
            && (model.discriminator.getMappedModels() == null
                || model.discriminator.getMappedModels().isEmpty());
    return hasOneOf && hasDiscriminatorNoMapping;
  }

  @NonNull
  public static Map<String, CodegenModel> modelMapListToModelClassMap(
      @NonNull final List<ModelMap> modelMapList) {
    return modelMapList.stream()
        .map(ModelMap::getModel)
        .collect(Collectors.toMap(CodegenModel::getClassname, Function.identity()));
  }

  @NonNull
  public static Map<String, CodegenModel> modelNameMapToModelClassMap(
      @NonNull final Map<String, CodegenModel> modelNameMap) {
    return modelNameMap.values().stream()
        .collect(Collectors.toMap(CodegenModel::getClassname, Function.identity()));
  }
}

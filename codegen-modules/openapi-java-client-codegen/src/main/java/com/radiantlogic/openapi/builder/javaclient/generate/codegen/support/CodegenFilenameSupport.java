package com.radiantlogic.openapi.builder.javaclient.generate.codegen.support;

import com.radiantlogic.openapi.builder.javaclient.generate.codegen.utils.CodegenConstants;
import com.radiantlogic.openapi.builder.javaclient.generate.codegen.utils.CodegenModelUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.model.ModelsMap;
import org.openapitools.codegen.utils.ModelUtils;

/**
 * There is an issue where anonymous schemas will be automatically assigned names by
 * openapi-generator. Those names may clash with names of schemas that were explicitly declared in
 * the spec. The generator doesn't catch these because they still end up differing by case, but many
 * filesystems don't support case-sensitive names and so when things are written out one will
 * overwrite the other and then the output is invalid.
 *
 * <p>This identifies and adds a suffix to names that will clash when being written out, so that
 * they are safe and everything will be written correctly.
 */
public class CodegenFilenameSupport {

  public void fixProblematicKeysForFilenames(
      @NonNull final Map<String, ModelsMap> allModelMaps,
      @NonNull final BiFunction<String, String, String> modelFilename) {
    final Map<String, ModelsMap> fixedModelMaps =
        allModelMaps.entrySet().stream()
            .map(entry -> createFilenameModelsMap(entry, modelFilename))
            .reduce(
                new HashMap<>(),
                (acc, singleEntryMap) ->
                    reduceFilenameModelsMaps(acc, singleEntryMap, allModelMaps))
            .values()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    // This is a tree map with a special comparator. Much better to do this modification than to
    // return a new one, it'll be less brittle and more reliable
    allModelMaps.clear();
    allModelMaps.putAll(fixedModelMaps);
  }

  @NonNull
  private static Map<String, Map.Entry<String, ModelsMap>> reduceFilenameModelsMaps(
      @NonNull Map<String, Map.Entry<String, ModelsMap>> acc,
      @NonNull Map<String, Map.Entry<String, ModelsMap>> singleEntryMap,
      @NonNull final Map<String, ModelsMap> allModelMaps) {
    final String fileBaseName = singleEntryMap.keySet().stream().findFirst().orElseThrow();
    final Map.Entry<String, ModelsMap> entry = singleEntryMap.get(fileBaseName);

    // If this name is unique in the map, we don't need to do anything
    if (!acc.containsKey(fileBaseName)) {
      acc.put(fileBaseName, entry);
      return acc;
    }

    // If the name is not unique, we need to make it unique with a suffix
    final CodegenModel model = ModelUtils.getModelByName(entry.getKey(), allModelMaps);
    int index = 0;
    String suffix = "";
    while (acc.containsKey(fileBaseName + suffix)) {
      index++;
      suffix = "V%d".formatted(index);
    }
    final String newFileBaseName = fileBaseName + suffix;
    final String newKey = entry.getKey() + suffix;
    final String oldClassName = model.classname;
    model.classname = model.classname + suffix;
    model.classFilename = model.classFilename + suffix;
    model.dataType = model.dataType + suffix;

    allModelMaps.values().stream()
        .map(CodegenModelUtils::extractModel)
        .filter(
            otherModel -> otherModel.imports != null && otherModel.imports.contains(oldClassName))
        .forEach(otherModel -> fixImports(model, otherModel, oldClassName, allModelMaps));

    acc.put(newFileBaseName, Map.entry(newKey, entry.getValue()));
    return acc;
  }

  private static void fixImports(
      @NonNull final CodegenModel model,
      @NonNull final CodegenModel otherModel,
      @NonNull final String oldClassName,
      @NonNull final Map<String, ModelsMap> allModelMaps) {
    otherModel.imports.remove(oldClassName);
    otherModel.imports.add(model.classname);

    ((List<Map<String, String>>)
            allModelMaps.get(otherModel.name).get(CodegenConstants.IMPORTS_KEY))
        .forEach(
            importMap -> {
              final String importValue = importMap.get(CodegenConstants.IMPORT_KEY);
              if (importValue.endsWith(".%s".formatted(oldClassName))) {
                final String newImportValue =
                    importValue.replaceAll(
                        "\\.%s$".formatted(oldClassName), ".%s".formatted(model.classname));
                importMap.put(CodegenConstants.IMPORT_KEY, newImportValue);
              }
            });
  }

  @NonNull
  private static Map<String, Map.Entry<String, ModelsMap>> createFilenameModelsMap(
      @NonNull final Map.Entry<String, ModelsMap> entry,
      @NonNull final BiFunction<String, String, String> modelFilename) {
    final String fileName = modelFilename.apply(CodegenConstants.MODEL_TEMPLATE, entry.getKey());
    final String fileBaseName = FilenameUtils.getBaseName(fileName).toLowerCase();

    return Map.of(fileBaseName, entry);
  }
}

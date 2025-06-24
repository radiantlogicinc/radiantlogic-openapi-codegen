package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support;

import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.utils.CodegenConstants;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.utils.CodegenModelUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ObjectUtils;
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

  @NonNull
  public Map<String, ModelsMap> fixProblematicKeysForFilenames(
      @NonNull final Map<String, ModelsMap> allModelMaps) {
    final Map<String, ModelsMap> fixedModelMaps =
        allModelMaps.entrySet().stream()
            .map(
                entry -> {
                  final String fileName = modelFilename("model.mustache", entry.getKey());
                  final String fileBaseName = FilenameUtils.getBaseName(fileName).toLowerCase();

                  return Map.of(fileBaseName, entry);
                })
            .reduce(
                new HashMap<>(),
                (acc, singleEntryMap) -> {
                  final String fileBaseName =
                      singleEntryMap.keySet().stream().findFirst().orElseThrow();
                  final Map.Entry<String, ModelsMap> entry = singleEntryMap.get(fileBaseName);

                  if (!acc.containsKey(fileBaseName)) {
                    acc.put(fileBaseName, entry);
                    return acc;
                  }

                  final CodegenModel model =
                      ModelUtils.getModelByName(entry.getKey(), allModelMaps);
                  int index = 1;
                  while (acc.containsKey(fileBaseName + index)) {
                    index++;
                  }
                  final String suffix = "V%d".formatted(index);
                  final String newFileBaseName = fileBaseName + suffix;
                  final String newKey = entry.getKey() + suffix;
                  final String oldClassName = model.classname;
                  model.classname = model.classname + suffix;
                  model.classFilename = model.classFilename + suffix;
                  model.dataType = model.dataType + suffix;

                  allModelMaps
                      .values()
                      .forEach(
                          otherModelMap -> {
                            final CodegenModel otherModel =
                                CodegenModelUtils.extractModel(otherModelMap);
                            if (otherModel.imports != null
                                && otherModel.imports.contains(oldClassName)) {
                              otherModel.imports.remove(oldClassName);
                              otherModel.imports.add(model.classname);

                              ((List<Map<String, String>>)
                                      allModelMaps
                                          .get(otherModel.name)
                                          .get(CodegenConstants.IMPORTS_KEY))
                                  .forEach(
                                      importMap -> {
                                        final String importValue =
                                            importMap.get(CodegenConstants.IMPORT_KEY);
                                        if (importValue.endsWith(".%s".formatted(oldClassName))) {
                                          final String newImportValue =
                                              importValue.replaceAll(
                                                  "\\.%s$".formatted(oldClassName),
                                                  ".%s".formatted(model.classname));
                                          importMap.put("import", newImportValue);
                                        }
                                      });
                            }
                          });

                  acc.put(newFileBaseName, Map.entry(newKey, entry.getValue()));
                  return acc;
                })
            .values()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    // The map created by DefaultGenerator is exactly like this, it must be the exact same type with
    // this comparator to work downstream
    final Map<String, ModelsMap> fixedModelMapsWithComparator =
        new TreeMap<>((o1, o2) -> ObjectUtils.compare(toModelName(o1), toModelName(o2)));

    fixedModelMapsWithComparator.putAll(fixedModelMaps);
    return fixedModelMapsWithComparator;
  }
}

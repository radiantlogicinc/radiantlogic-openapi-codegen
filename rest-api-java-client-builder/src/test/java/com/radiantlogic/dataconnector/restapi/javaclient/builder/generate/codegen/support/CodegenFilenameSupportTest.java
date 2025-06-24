package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.utils.CodegenConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.ModelsMap;

public class CodegenFilenameSupportTest {
  @Test
  void itDoesNothingIfNoFilenamesClash() {
    // Create two models with different names (no clash)
    final CodegenModel model1 = createCodegenModel("Model1", "Model1");
    final CodegenModel model2 = createCodegenModel("Model2", "Model2");

    // Create ModelsMap for each model
    final ModelsMap modelsMap1 = createModelsMap(model1);
    final ModelsMap modelsMap2 = createModelsMap(model2);

    // Create the input map
    final Map<String, ModelsMap> allModelMaps = new HashMap<>();
    allModelMaps.put("Model1", modelsMap1);
    allModelMaps.put("Model2", modelsMap2);

    // Create a copy of the input map to compare later
    final Map<String, ModelsMap> expectedModelMaps = new HashMap<>(allModelMaps);

    // Call the method under test
    final CodegenFilenameSupport support = new CodegenFilenameSupport();
    support.fixProblematicKeysForFilenames(allModelMaps, this::modelFilename);

    // Assert that the map is unchanged
    assertThat(allModelMaps).isEqualTo(expectedModelMaps);

    // Assert that the models are unchanged
    final CodegenModel resultModel1 = extractModelFromMap(allModelMaps, "Model1");
    final CodegenModel resultModel2 = extractModelFromMap(allModelMaps, "Model2");

    assertThat(resultModel1.name).isEqualTo("Model1");
    assertThat(resultModel1.classname).isEqualTo("Model1");
    assertThat(resultModel2.name).isEqualTo("Model2");
    assertThat(resultModel2.classname).isEqualTo("Model2");
  }

  @Test
  void itAddsSuffixToNameAndFixesImportsIfClash() {
    // Create two models with names that will clash when lowercased
    final CodegenModel model1 = createCodegenModel("model", "Model");
    final CodegenModel model2 = createCodegenModel("MODEL", "MODEL");

    // Add an import from model2 to model1
    model2.imports = new java.util.HashSet<>();
    model2.imports.add("Model");

    // Create ModelsMap for each model
    final ModelsMap modelsMap1 = createModelsMap(model1);
    final ModelsMap modelsMap2 = createModelsMap(model2);

    // Add import map to modelsMap2
    final List<Map<String, String>> imports = new ArrayList<>();
    final Map<String, String> importMap = new HashMap<>();
    importMap.put(CodegenConstants.IMPORT_KEY, "com.example.Model");
    imports.add(importMap);
    modelsMap2.put(CodegenConstants.IMPORTS_KEY, imports);

    // Create the input map
    final Map<String, ModelsMap> allModelMaps = new HashMap<>();
    allModelMaps.put("model", modelsMap1);
    allModelMaps.put("MODEL", modelsMap2);

    // Call the method under test
    final CodegenFilenameSupport support = new CodegenFilenameSupport();
    support.fixProblematicKeysForFilenames(allModelMaps, this::modelFilename);

    // Assert that one of the models has been renamed with a suffix
    assertThat(allModelMaps).hasSize(2);

    // Check if MODEL was renamed to MODELV1
    final boolean modelRenamed = allModelMaps.containsKey("MODELV1");

    // MODEL was renamed to MODELV1
    final CodegenModel resultModel1 = extractModelFromMap(allModelMaps, "model");
    final CodegenModel resultModel2 = extractModelFromMap(allModelMaps, "MODELV1");

    // Check model1 is unchanged
    assertThat(resultModel1.name).isEqualTo("model");
    assertThat(resultModel1.classname).isEqualTo("Model");

    // Check model2 has suffix
    assertThat(resultModel2.name).isEqualTo("MODELV1");
    assertThat(resultModel2.classname).isEqualTo("MODELV1");

    // Check imports were updated
    assertThat(resultModel2.imports).contains("MODELV1");
    assertThat(resultModel2.imports).doesNotContain("Model");

    // Check import map was updated
    final List<Map<String, String>> resultImports =
        (List<Map<String, String>>) allModelMaps.get("MODELV1").get(CodegenConstants.IMPORTS_KEY);
    assertThat(resultImports).hasSize(1);
    assertThat(resultImports.get(0).get(CodegenConstants.IMPORT_KEY))
        .isEqualTo("com.example.MODELV1");
  }

  @NonNull
  private String modelFilename(
      @NonNull final String templateName, @NonNull final String modelName) {
    return modelName;
  }

  @NonNull
  private CodegenModel createCodegenModel(
      @NonNull final String name, @NonNull final String classname) {
    final CodegenModel model = new CodegenModel();
    model.name = name;
    model.classname = classname;
    model.classFilename = classname;
    model.dataType = classname;
    return model;
  }

  @NonNull
  private ModelsMap createModelsMap(@NonNull final CodegenModel model) {
    final ModelMap modelMap = new ModelMap();
    modelMap.setModel(model);

    final ModelsMap modelsMap = new ModelsMap();
    modelsMap.setModels(List.of(modelMap));
    return modelsMap;
  }

  @NonNull
  private CodegenModel extractModelFromMap(
      @NonNull final Map<String, ModelsMap> allModelMaps, @NonNull final String key) {
    final ModelsMap modelsMap = allModelMaps.get(key);
    return modelsMap.getModels().get(0).getModel();
  }
}

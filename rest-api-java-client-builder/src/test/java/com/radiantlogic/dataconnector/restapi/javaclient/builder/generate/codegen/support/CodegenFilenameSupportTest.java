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
import org.openapitools.codegen.utils.ModelUtils;

public class CodegenFilenameSupportTest {
  @Test
  void itDoesNothingIfNoFilenamesClash() {
    final CodegenModel model1 = createCodegenModel("Model1", "Model1");
    final CodegenModel model2 = createCodegenModel("Model2", "Model2");

    final ModelsMap modelsMap1 = createModelsMap(model1);
    final ModelsMap modelsMap2 = createModelsMap(model2);

    final Map<String, ModelsMap> allModelMaps = new HashMap<>();
    allModelMaps.put("Model1", modelsMap1);
    allModelMaps.put("Model2", modelsMap2);

    final Map<String, ModelsMap> expectedModelMaps = new HashMap<>(allModelMaps);

    final CodegenFilenameSupport support = new CodegenFilenameSupport();
    support.fixProblematicKeysForFilenames(allModelMaps, this::modelFilename);

    assertThat(allModelMaps).isEqualTo(expectedModelMaps);

    final CodegenModel resultModel1 = ModelUtils.getModelByName("Model1", allModelMaps);
    final CodegenModel resultModel2 = ModelUtils.getModelByName("Model2", allModelMaps);

    assertThat(resultModel1.name).isEqualTo("Model1");
    assertThat(resultModel1.classname).isEqualTo("Model1");
    assertThat(resultModel2.name).isEqualTo("Model2");
    assertThat(resultModel2.classname).isEqualTo("Model2");
  }

  @Test
  void itAddsSuffixToNameAndFixesImportsIfClash() {
    final CodegenModel model1 = createCodegenModel("model", "Model");
    final CodegenModel model2 = createCodegenModel("MODEL", "MODEL");

    model2.imports = new java.util.HashSet<>();
    model2.imports.add("Model");

    final ModelsMap modelsMap1 = createModelsMap(model1);
    final ModelsMap modelsMap2 = createModelsMap(model2);

    final List<Map<String, String>> imports = new ArrayList<>();
    final Map<String, String> importMap = new HashMap<>();
    importMap.put(CodegenConstants.IMPORT_KEY, "com.example.Model");
    imports.add(importMap);
    modelsMap2.put(CodegenConstants.IMPORTS_KEY, imports);

    final Map<String, ModelsMap> allModelMaps = new HashMap<>();
    allModelMaps.put("model", modelsMap1);
    allModelMaps.put("MODEL", modelsMap2);

    final CodegenFilenameSupport support = new CodegenFilenameSupport();
    support.fixProblematicKeysForFilenames(allModelMaps, this::modelFilename);

    assertThat(allModelMaps).hasSize(2);

    final boolean modelRenamed = allModelMaps.containsKey("MODELV1");
    assertThat(modelRenamed).isTrue();

    final CodegenModel resultModel1 = ModelUtils.getModelByName("model", allModelMaps);
    final CodegenModel resultModel2 = ModelUtils.getModelByName("MODELV1", allModelMaps);

    assertThat(resultModel1.name).isEqualTo("model");
    assertThat(resultModel1.classname).isEqualTo("Model");

    assertThat(resultModel2.name).isEqualTo("MODELV1");
    assertThat(resultModel2.classname).isEqualTo("MODELV1");

    assertThat(resultModel2.imports).contains("MODELV1");
    assertThat(resultModel2.imports).doesNotContain("Model");

    final List<Map<String, String>> resultImports =
        (List<Map<String, String>>) allModelMaps.get("MODELV1").get(CodegenConstants.IMPORTS_KEY);
    assertThat(resultImports).hasSize(1);
    assertThat(resultImports.get(0).get(CodegenConstants.IMPORT_KEY))
        .isEqualTo("com.example.MODELV1");
  }

  @NonNull
  private String modelFilename(
      @NonNull final String templateName, @NonNull final String modelName) {
    return "/foo/bar/%s.java".formatted(modelName);
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
}

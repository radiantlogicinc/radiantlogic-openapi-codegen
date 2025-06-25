package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.utils.CodegenConstants;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.utils.CodegenModelUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenModel;
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
    model1.imports.add(model2.classname);

    model2.imports = new java.util.HashSet<>();
    model2.imports.add("Model");

    final ModelsMap modelsMap1 = createModelsMap(model1);
    final ModelsMap modelsMap2 = createModelsMap(model2);

    final List<Map<String, String>> imports = new ArrayList<>();
    final Map<String, String> importMap = new HashMap<>();
    importMap.put(CodegenConstants.IMPORT_KEY, "com.example.MODEL");
    imports.add(importMap);
    modelsMap1.put(CodegenConstants.IMPORTS_KEY, imports);

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
    assertThat(resultModel1.classFilename).isEqualTo("Model");

    assertThat(resultModel2.name).isEqualTo("MODEL");
    assertThat(resultModel2.classname).isEqualTo("MODELV1");
    assertThat(resultModel2.classFilename).isEqualTo("MODELV1");

    final List<Map<String, String>> resultImports =
        (List<Map<String, String>>) allModelMaps.get("model").get(CodegenConstants.IMPORTS_KEY);
    assertThat(resultImports).hasSize(1);
    assertThat(resultImports.get(0).get(CodegenConstants.IMPORT_KEY))
        .isEqualTo("com.radiantlogic.MODELV1");
    assertThat(resultModel1.imports).hasSize(1).contains("MODELV1");
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
    model.imports = new HashSet<>();
    return model;
  }

  private ModelsMap createModelsMap(@NonNull final CodegenModel codegenModel) {
    final ModelsMap baseModelsMap = new ModelsMap();
    final List<String> importList =
        codegenModel.imports.stream().map("com.radiantlogic.%s"::formatted).toList();
    baseModelsMap.put(CodegenConstants.IMPORTS_KEY, new ArrayList<>(importList));
    return CodegenModelUtils.wrapInModelsMap(baseModelsMap, "com.radiantlogic", codegenModel);
  }
}

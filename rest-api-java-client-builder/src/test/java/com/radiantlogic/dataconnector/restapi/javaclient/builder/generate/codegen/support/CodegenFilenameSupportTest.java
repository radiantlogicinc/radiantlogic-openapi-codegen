package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.utils.CodegenConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.ModelsMap;

public class CodegenFilenameSupportTest {
  private CodegenFilenameSupport codegenFilenameSupport;
  private BiFunction<String, String, String> modelFilename;

  @BeforeEach
  void setUp() {
    codegenFilenameSupport = new CodegenFilenameSupport();
    modelFilename = (template, name) -> name + ".java";
  }

  @Test
  void itDoesNothingWhenNoClashes() {
    // Arrange
    final Map<String, ModelsMap> allModelMaps = new TreeMap<>();

    final ModelsMap modelsMap1 = createModelsMap("Model1", "Model1");
    final ModelsMap modelsMap2 = createModelsMap("Model2", "Model2");

    allModelMaps.put("Model1", modelsMap1);
    allModelMaps.put("Model2", modelsMap2);

    final Map<String, ModelsMap> originalModelMaps = new HashMap<>(allModelMaps);

    // Act
    codegenFilenameSupport.fixProblematicKeysForFilenames(allModelMaps, modelFilename);

    // Assert
    assertThat(allModelMaps).isEqualTo(originalModelMaps);
  }

  @Test
  void itFixesClashingFilenames() {
    // Arrange
    final Map<String, ModelsMap> allModelMaps = new TreeMap<>();

    // Create two models with names that will clash when lowercased
    final ModelsMap modelsMap1 = createModelsMap("model", "model");
    final ModelsMap modelsMap2 = createModelsMap("Model", "Model");

    allModelMaps.put("model", modelsMap1);
    allModelMaps.put("Model", modelsMap2);

    // Act
    codegenFilenameSupport.fixProblematicKeysForFilenames(allModelMaps, modelFilename);

    // Assert
    assertThat(allModelMaps).hasSize(2);

    // One of the models should have been renamed with a suffix
    boolean foundOriginal = false;
    boolean foundRenamed = false;

    for (Map.Entry<String, ModelsMap> entry : allModelMaps.entrySet()) {
      final String key = entry.getKey();
      final ModelsMap value = entry.getValue();
      final CodegenModel model = extractModel(value);

      if ((key.equals("model") && model.classname.equals("model"))
          || (key.equals("Model") && model.classname.equals("Model"))) {
        foundOriginal = true;
      } else if ((key.matches(".*V\\d+") && model.classname.matches(".*V\\d+"))) {
        foundRenamed = true;
        // Verify that the renamed model has updated properties
        assertThat(model.dataType).isEqualTo(model.classname);
        assertThat(model.classFilename).isEqualTo(model.classname);
      }
    }

    assertThat(foundOriginal).isTrue();
    assertThat(foundRenamed).isTrue();
  }

  @Test
  void itFixesMultipleClashingFilenames() {
    // Arrange
    final Map<String, ModelsMap> allModelMaps = new TreeMap<>();

    // Create three models with names that will clash when lowercased
    final ModelsMap modelsMap1 = createModelsMap("test", "test");
    final ModelsMap modelsMap2 = createModelsMap("Test", "Test");
    final ModelsMap modelsMap3 = createModelsMap("TEST", "TEST");

    allModelMaps.put("test", modelsMap1);
    allModelMaps.put("Test", modelsMap2);
    allModelMaps.put("TEST", modelsMap3);

    // Act
    codegenFilenameSupport.fixProblematicKeysForFilenames(allModelMaps, modelFilename);

    // Assert
    assertThat(allModelMaps).hasSize(3);

    // Two of the models should have been renamed with suffixes
    int originalCount = 0;
    int renamedCount = 0;

    for (Map.Entry<String, ModelsMap> entry : allModelMaps.entrySet()) {
      final CodegenModel model = extractModel(entry.getValue());

      if (model.classname.matches(".*V\\d+")) {
        renamedCount++;
        // Verify that the renamed model has updated properties
        assertThat(model.dataType).isEqualTo(model.classname);
        assertThat(model.classFilename).isEqualTo(model.classname);
      } else {
        originalCount++;
      }
    }

    assertThat(originalCount).isEqualTo(1);
    assertThat(renamedCount).isEqualTo(2);
  }

  @Test
  void itHandlesEmptyMap() {
    // Arrange
    final Map<String, ModelsMap> allModelMaps = new TreeMap<>();

    // Act
    codegenFilenameSupport.fixProblematicKeysForFilenames(allModelMaps, modelFilename);

    // Assert
    assertThat(allModelMaps).isEmpty();
  }

  @Test
  void itFixesImportsWhenRenamingModels() {
    // Arrange
    final Map<String, ModelsMap> allModelMaps = new TreeMap<>();

    // Create two models with names that will clash when lowercased
    final ModelsMap modelsMap1 = createModelsMap("model", "model");
    final ModelsMap modelsMap2 = createModelsMap("Model", "Model");

    // Add an import from model2 to model1
    final CodegenModel model2 = extractModel(modelsMap2);
    model2.imports = new HashSet<>();
    model2.imports.add("model");

    // Add import map to modelsMap2
    final List<Map<String, String>> importMaps = new ArrayList<>();
    final Map<String, String> importMap = new HashMap<>();
    importMap.put(CodegenConstants.IMPORT_KEY, "com.example.model");
    importMaps.add(importMap);
    modelsMap2.put(CodegenConstants.IMPORTS_KEY, importMaps);

    allModelMaps.put("model", modelsMap1);
    allModelMaps.put("Model", modelsMap2);

    // Act
    codegenFilenameSupport.fixProblematicKeysForFilenames(allModelMaps, modelFilename);

    // Assert
    // Find the renamed model
    String renamedModelName = null;
    for (String key : allModelMaps.keySet()) {
      if (key.matches("model(V\\d+)") || key.matches("Model(V\\d+)")) {
        renamedModelName = key;
        break;
      }
    }

    assertThat(renamedModelName).isNotNull();

    // Check that the import in the other model has been updated
    for (Map.Entry<String, ModelsMap> entry : allModelMaps.entrySet()) {
      if (!entry.getKey().equals(renamedModelName)) {
        final CodegenModel model = extractModel(entry.getValue());
        if (model.imports != null && !model.imports.isEmpty()) {
          // The import should have been updated to the new name
          assertThat(model.imports).doesNotContain("model").doesNotContain("Model");

          // Check that the import map was updated
          final List<Map<String, String>> imports =
              (List<Map<String, String>>) entry.getValue().get(CodegenConstants.IMPORTS_KEY);
          if (imports != null && !imports.isEmpty()) {
            for (Map<String, String> imp : imports) {
              final String importValue = imp.get(CodegenConstants.IMPORT_KEY);
              assertThat(importValue).doesNotEndWith(".model").doesNotEndWith(".Model");
            }
          }
        }
      }
    }
  }

  private ModelsMap createModelsMap(final String modelName, final String className) {
    final CodegenModel model = new CodegenModel();
    model.name = modelName;
    model.classname = className;
    model.classFilename = className;
    model.dataType = className;

    final ModelMap modelMap = new ModelMap();
    modelMap.setModel(model);

    final ModelsMap modelsMap = new ModelsMap();
    modelsMap.setModels(List.of(modelMap));

    return modelsMap;
  }

  private CodegenModel extractModel(final ModelsMap modelsMap) {
    return modelsMap.getModels().get(0).getModel();
  }
}

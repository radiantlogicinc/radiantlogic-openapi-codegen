package com.radiantlogic.openapi.builder.javaclient.generate.codegen.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.radiantlogic.openapi.builder.javaclient.generate.codegen.utils.CodegenConstants;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.ModelsMap;

public class CodegenNewEnumProcessorSupportTest {
  @Test
  void itProcessesAndMergesAllNewAndUniqueEnums() {
    // Arrange
    final CodegenModel enumModel1 = new CodegenModel();
    enumModel1.name = "StatusEnum";
    enumModel1.classname = "StatusEnum";
    enumModel1.isEnum = true;
    enumModel1.allowableValues =
        Map.of(
            CodegenConstants.ENUM_VARS_KEY,
                List.of(
                    Map.of(
                        CodegenConstants.NAME_KEY, "ACTIVE", CodegenConstants.VALUE_KEY, "active"),
                    Map.of(
                        CodegenConstants.NAME_KEY,
                        "INACTIVE",
                        CodegenConstants.VALUE_KEY,
                        "inactive")),
            CodegenConstants.VALUES_KEY, List.of("active", "inactive"));

    final CodegenModel enumModel2 = new CodegenModel();
    enumModel2.name = "TypeEnum";
    enumModel2.classname = "TypeEnum";
    enumModel2.isEnum = true;
    enumModel2.allowableValues =
        Map.of(
            CodegenConstants.ENUM_VARS_KEY,
                List.of(
                    Map.of(CodegenConstants.NAME_KEY, "TYPE1", CodegenConstants.VALUE_KEY, "type1"),
                    Map.of(
                        CodegenConstants.NAME_KEY, "TYPE2", CodegenConstants.VALUE_KEY, "type2")),
            CodegenConstants.VALUES_KEY, List.of("type1", "type2"));

    final List<CodegenModel> newEnums = List.of(enumModel1, enumModel2);

    // Create a base ModelsMap
    final ModelsMap baseModelsMap = new ModelsMap();
    final List<Map<String, String>> imports = new ArrayList<>();
    imports.add(Map.of(CodegenConstants.IMPORT_KEY, "java.util.List"));
    baseModelsMap.put(CodegenConstants.IMPORTS_KEY, imports);

    // Create the allModelMaps map with a base entry
    final Map<String, ModelsMap> allModelMaps = new HashMap<>();
    allModelMaps.put("BaseModel", baseModelsMap);

    final String modelPackage = "com.example.model";
    final Map<String, String> importMapping =
        Map.of(
            "List", "java.util.List",
            "Map", "java.util.Map");

    // Act
    final CodegenNewEnumProcessorSupport support = new CodegenNewEnumProcessorSupport();
    support.processNewEnumsAndMergeToModelMaps(newEnums, allModelMaps, modelPackage, importMapping);

    // Assert
    assertThat(allModelMaps).hasSize(3); // BaseModel + 2 new enums
    assertThat(allModelMaps).containsKeys("BaseModel", "StatusEnum", "TypeEnum");

    // Verify StatusEnum was added correctly
    final ModelsMap statusEnumModelsMap = allModelMaps.get("StatusEnum");
    assertThat(statusEnumModelsMap).isNotNull();
    assertThat(statusEnumModelsMap.getModels()).hasSize(1);

    final CodegenModel statusEnumModel = statusEnumModelsMap.getModels().get(0).getModel();
    assertThat(statusEnumModel.name).isEqualTo("StatusEnum");
    assertThat(statusEnumModel.isEnum).isTrue();

    @SuppressWarnings("unchecked")
    final Collection<Map<String, Object>> statusEnumVars =
        (Collection<Map<String, Object>>)
            statusEnumModel.allowableValues.get(CodegenConstants.ENUM_VARS_KEY);
    assertThat(statusEnumVars).hasSize(2);

    // Verify enum values are present
    final List<String> statusEnumNames =
        statusEnumVars.stream().map(map -> (String) map.get(CodegenConstants.NAME_KEY)).toList();
    assertThat(statusEnumNames).containsExactlyInAnyOrder("ACTIVE", "INACTIVE");

    // Verify TypeEnum was added correctly
    final ModelsMap typeEnumModelsMap = allModelMaps.get("TypeEnum");
    assertThat(typeEnumModelsMap).isNotNull();
    assertThat(typeEnumModelsMap.getModels()).hasSize(1);

    final CodegenModel typeEnumModel = typeEnumModelsMap.getModels().get(0).getModel();
    assertThat(typeEnumModel.name).isEqualTo("TypeEnum");
    assertThat(typeEnumModel.isEnum).isTrue();

    @SuppressWarnings("unchecked")
    final Collection<Map<String, Object>> typeEnumVars =
        (Collection<Map<String, Object>>)
            typeEnumModel.allowableValues.get(CodegenConstants.ENUM_VARS_KEY);
    assertThat(typeEnumVars).hasSize(2);

    // Verify enum values are present
    final List<String> typeEnumNames =
        typeEnumVars.stream().map(map -> (String) map.get(CodegenConstants.NAME_KEY)).toList();
    assertThat(typeEnumNames).containsExactlyInAnyOrder("TYPE1", "TYPE2");

    // Verify enum values
    @SuppressWarnings("unchecked")
    final List<Object> typeEnumValues =
        (List<Object>) typeEnumModel.allowableValues.get(CodegenConstants.VALUES_KEY);
    assertThat(typeEnumValues).containsExactlyInAnyOrder("type1", "type2");

    // Verify imports were added correctly
    assertThat(statusEnumModelsMap.get(CodegenConstants.IMPORTS_KEY)).isNotNull();
    assertThat(typeEnumModelsMap.get(CodegenConstants.IMPORTS_KEY)).isNotNull();
  }

  @Test
  void itProcessesAndMergesEnumsWithDuplicates() {
    // Arrange
    final CodegenModel enumModel1 = new CodegenModel();
    enumModel1.name = "StatusEnum";
    enumModel1.classname = "StatusEnum";
    enumModel1.isEnum = true;
    enumModel1.allowableValues =
        Map.of(
            CodegenConstants.ENUM_VARS_KEY,
                List.of(
                    Map.of(
                        CodegenConstants.NAME_KEY, "ACTIVE", CodegenConstants.VALUE_KEY, "active"),
                    Map.of(
                        CodegenConstants.NAME_KEY,
                        "INACTIVE",
                        CodegenConstants.VALUE_KEY,
                        "inactive")),
            CodegenConstants.VALUES_KEY, List.of("active", "inactive"));

    final CodegenModel enumModel2 = new CodegenModel();
    enumModel2.name = "StatusEnum"; // Same name as enumModel1
    enumModel2.classname = "StatusEnum";
    enumModel2.isEnum = true;
    enumModel2.allowableValues =
        Map.of(
            CodegenConstants.ENUM_VARS_KEY,
                List.of(
                    Map.of(
                        CodegenConstants.NAME_KEY,
                        "PENDING",
                        CodegenConstants.VALUE_KEY,
                        "pending"),
                    Map.of(
                        CodegenConstants.NAME_KEY,
                        "COMPLETED",
                        CodegenConstants.VALUE_KEY,
                        "completed")),
            CodegenConstants.VALUES_KEY, List.of("pending", "completed"));

    final List<CodegenModel> newEnums = List.of(enumModel1, enumModel2);

    // Create a base ModelsMap
    final ModelsMap baseModelsMap = new ModelsMap();
    final List<Map<String, String>> imports = new ArrayList<>();
    imports.add(Map.of(CodegenConstants.IMPORT_KEY, "java.util.List"));
    baseModelsMap.put(CodegenConstants.IMPORTS_KEY, imports);

    // Create the allModelMaps map with a base entry
    final Map<String, ModelsMap> allModelMaps = new HashMap<>();
    allModelMaps.put("BaseModel", baseModelsMap);

    final String modelPackage = "com.example.model";
    final Map<String, String> importMapping =
        Map.of(
            "List", "java.util.List",
            "Map", "java.util.Map");

    // Act
    final CodegenNewEnumProcessorSupport support = new CodegenNewEnumProcessorSupport();
    support.processNewEnumsAndMergeToModelMaps(newEnums, allModelMaps, modelPackage, importMapping);

    // Assert
    assertThat(allModelMaps).hasSize(2); // BaseModel + 1 merged enum
    assertThat(allModelMaps).containsKeys("BaseModel", "StatusEnum");

    // Verify StatusEnum was merged correctly
    final ModelsMap statusEnumModelsMap = allModelMaps.get("StatusEnum");
    assertThat(statusEnumModelsMap).isNotNull();
    assertThat(statusEnumModelsMap.getModels()).hasSize(1);

    final CodegenModel statusEnumModel = statusEnumModelsMap.getModels().get(0).getModel();
    assertThat(statusEnumModel.name).isEqualTo("StatusEnum");
    assertThat(statusEnumModel.isEnum).isTrue();

    @SuppressWarnings("unchecked")
    final Collection<Map<String, Object>> statusEnumVars =
        (Collection<Map<String, Object>>)
            statusEnumModel.allowableValues.get(CodegenConstants.ENUM_VARS_KEY);
    assertThat(statusEnumVars).hasSize(4); // All enum values from both models

    // Verify all enum values are present
    final List<String> enumNames =
        statusEnumVars.stream().map(map -> (String) map.get(CodegenConstants.NAME_KEY)).toList();
    assertThat(enumNames).containsExactlyInAnyOrder("ACTIVE", "INACTIVE", "PENDING", "COMPLETED");

    // Verify all enum values are present
    @SuppressWarnings("unchecked")
    final List<Object> enumValues =
        (List<Object>) statusEnumModel.allowableValues.get(CodegenConstants.VALUES_KEY);
    assertThat(enumValues).containsExactlyInAnyOrder("active", "inactive", "pending", "completed");
  }

  @Test
  void itProcessesAndMergesEnumsThatAlreadyExist() {
    // Arrange
    // Create an existing enum model
    final CodegenModel existingEnumModel = new CodegenModel();
    existingEnumModel.name = "StatusEnum";
    existingEnumModel.classname = "StatusEnum";
    existingEnumModel.isEnum = true;
    existingEnumModel.allowableValues =
        Map.of(
            CodegenConstants.ENUM_VARS_KEY,
                List.of(
                    Map.of(
                        CodegenConstants.NAME_KEY, "ACTIVE", CodegenConstants.VALUE_KEY, "active"),
                    Map.of(
                        CodegenConstants.NAME_KEY,
                        "INACTIVE",
                        CodegenConstants.VALUE_KEY,
                        "inactive")),
            CodegenConstants.VALUES_KEY, List.of("active", "inactive"));

    // Create a ModelsMap for the existing enum
    final ModelsMap existingEnumModelsMap = new ModelsMap();
    final ModelMap existingModelMap = new ModelMap();
    existingModelMap.setModel(existingEnumModel);
    existingEnumModelsMap.setModels(List.of(existingModelMap));

    // Create a base ModelsMap
    final ModelsMap baseModelsMap = new ModelsMap();
    final List<Map<String, String>> imports = new ArrayList<>();
    imports.add(Map.of(CodegenConstants.IMPORT_KEY, "java.util.List"));
    baseModelsMap.put(CodegenConstants.IMPORTS_KEY, imports);

    // Create the allModelMaps map with the base and existing enum
    final Map<String, ModelsMap> allModelMaps = new HashMap<>();
    allModelMaps.put("BaseModel", baseModelsMap);
    allModelMaps.put("StatusEnum", existingEnumModelsMap);

    // Create a new enum model with the same name but different values
    final CodegenModel newEnumModel = new CodegenModel();
    newEnumModel.name = "StatusEnum"; // Same name as existing enum
    newEnumModel.classname = "StatusEnum";
    newEnumModel.isEnum = true;
    newEnumModel.allowableValues =
        Map.of(
            CodegenConstants.ENUM_VARS_KEY,
                List.of(
                    Map.of(
                        CodegenConstants.NAME_KEY,
                        "PENDING",
                        CodegenConstants.VALUE_KEY,
                        "pending"),
                    Map.of(
                        CodegenConstants.NAME_KEY,
                        "COMPLETED",
                        CodegenConstants.VALUE_KEY,
                        "completed")),
            CodegenConstants.VALUES_KEY, List.of("pending", "completed"));

    final List<CodegenModel> newEnums = List.of(newEnumModel);

    final String modelPackage = "com.example.model";
    final Map<String, String> importMapping =
        Map.of(
            "List", "java.util.List",
            "Map", "java.util.Map");

    // Act
    final CodegenNewEnumProcessorSupport support = new CodegenNewEnumProcessorSupport();
    support.processNewEnumsAndMergeToModelMaps(newEnums, allModelMaps, modelPackage, importMapping);

    // Assert
    assertThat(allModelMaps).hasSize(2); // BaseModel + merged StatusEnum

    // Verify StatusEnum was merged correctly with the existing enum
    final ModelsMap statusEnumModelsMap = allModelMaps.get("StatusEnum");
    assertThat(statusEnumModelsMap).isNotNull();
    assertThat(statusEnumModelsMap.getModels()).hasSize(1);

    final CodegenModel mergedEnumModel = statusEnumModelsMap.getModels().get(0).getModel();
    assertThat(mergedEnumModel.name).isEqualTo("StatusEnum");
    assertThat(mergedEnumModel.isEnum).isTrue();

    @SuppressWarnings("unchecked")
    final Collection<Map<String, Object>> mergedEnumVars =
        (Collection<Map<String, Object>>)
            mergedEnumModel.allowableValues.get(CodegenConstants.ENUM_VARS_KEY);
    assertThat(mergedEnumVars).hasSize(4); // All enum values from both models

    // Verify all enum values are present
    final List<String> enumNames =
        mergedEnumVars.stream().map(map -> (String) map.get(CodegenConstants.NAME_KEY)).toList();
    assertThat(enumNames).containsExactlyInAnyOrder("ACTIVE", "INACTIVE", "PENDING", "COMPLETED");

    // Verify all enum values are present
    @SuppressWarnings("unchecked")
    final List<Object> enumValues =
        (List<Object>) mergedEnumModel.allowableValues.get(CodegenConstants.VALUES_KEY);
    assertThat(enumValues).containsExactlyInAnyOrder("active", "inactive", "pending", "completed");
  }
}

package com.radiantlogic.openapi.codegen.javaclient.generate.codegen.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.radiantlogic.openapi.codegen.javaclient.exceptions.ModelNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenDiscriminator;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.ModelsMap;

public class CodegenModelUtilsTest {

  @Nested
  class HasDiscriminatorChildren {
    @Test
    void itHasNoDiscriminator() {
      final CodegenModel model = new CodegenModel();
      assertThat(CodegenModelUtils.hasDiscriminatorChildren(model)).isFalse();
    }

    @Test
    void itHasDiscriminatorWithNoMappings() {
      final CodegenModel model = new CodegenModel();
      model.discriminator = new CodegenDiscriminator();
      model.discriminator.setMappedModels(null);
      assertThat(CodegenModelUtils.hasDiscriminatorChildren(model)).isFalse();
    }

    @Test
    void itHasDiscriminatorWithMappings() {
      final CodegenModel model = new CodegenModel();
      model.discriminator = new CodegenDiscriminator();
      model.discriminator.setMappedModels(Set.of());
      assertThat(CodegenModelUtils.hasDiscriminatorChildren(model)).isTrue();
    }
  }

  @Nested
  class ExtractModel {
    @Test
    void itHasNoModelsToExtract() {
      final ModelsMap modelsMap = new ModelsMap();
      modelsMap.setModels(List.of());
      assertThatThrownBy(() -> CodegenModelUtils.extractModel(modelsMap))
          .isInstanceOf(ModelNotFoundException.class);
    }

    @Test
    void itHasTooManyModelsToExtract() {
      final ModelsMap modelsMap = new ModelsMap();
      modelsMap.setModels(List.of(new ModelMap(), new ModelMap()));
      assertThatThrownBy(() -> CodegenModelUtils.extractModel(modelsMap))
          .isInstanceOf(ModelNotFoundException.class);
    }

    @Test
    void itExtractsModel() {
      final CodegenModel model = new CodegenModel();
      model.name = "MyModel";
      final ModelMap modelMap = new ModelMap();
      modelMap.setModel(model);

      final ModelsMap modelsMap = new ModelsMap();
      modelsMap.setModels(List.of(modelMap));

      final CodegenModel actualModel = CodegenModelUtils.extractModel(modelsMap);
      assertThat(actualModel).isEqualTo(model);
    }
  }

  @Nested
  class WrapInModelsMap {
    @Test
    void itWrapsInModelMap() {
      final CodegenModel model = new CodegenModel();
      model.name = "MyModel";
      model.classname = "MyModelClass";

      final String packageName = "com.radiantlogic";

      final ModelsMap base = new ModelsMap();
      base.put("hello", "world");

      final ModelsMap expected = new ModelsMap();
      expected.putAll(base);
      final ModelMap modelMap = new ModelMap();
      modelMap.setModel(model);
      modelMap.put(
          CodegenConstants.IMPORT_PATH_KEY, "%s.%s".formatted(packageName, model.classname));
      expected.setModels(List.of(modelMap));

      final ModelsMap actual = CodegenModelUtils.wrapInModelsMap(base, packageName, model);
      assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }
  }

  @Nested
  class HasNonDiscriminatorChildren {
    @Test
    void itHasOneOfChildrenWithUnmappedDiscriminator() {
      final CodegenModel model = new CodegenModel();
      model.oneOf = Set.of("Child1", "Child2");
      model.discriminator = new CodegenDiscriminator();
      model.discriminator.setMappedModels(null);

      assertThat(CodegenModelUtils.hasNonDiscriminatorChildren(model)).isTrue();
    }

    @Test
    void itHasOneOfChildrenWithMappedDiscriminator() {
      final CodegenModel model = new CodegenModel();
      model.oneOf = Set.of("Child1", "Child2");
      model.discriminator = new CodegenDiscriminator();
      model.discriminator.setMappedModels(
          Set.of(new CodegenDiscriminator.MappedModel("Child1", "Child1Model", true)));

      assertThat(CodegenModelUtils.hasNonDiscriminatorChildren(model)).isFalse();
    }

    @Test
    void itHasNoOneOfChildren() {
      final CodegenModel model = new CodegenModel();
      model.oneOf = null;

      assertThat(CodegenModelUtils.hasNonDiscriminatorChildren(model)).isFalse();
    }
  }

  @Nested
  class ModelMapListToModelClassMap {
    @Test
    void itConvertsListToMap() {
      // Create test models
      final CodegenModel model1 = new CodegenModel();
      model1.classname = "Model1";

      final CodegenModel model2 = new CodegenModel();
      model2.classname = "Model2";

      // Create ModelMaps
      final ModelMap modelMap1 = new ModelMap();
      modelMap1.setModel(model1);

      final ModelMap modelMap2 = new ModelMap();
      modelMap2.setModel(model2);

      // Create list of ModelMaps
      final List<ModelMap> modelMapList = List.of(modelMap1, modelMap2);

      // Expected map - create manually to avoid Map.of() which might not be available
      final Map<String, CodegenModel> expected = new java.util.HashMap<>();
      expected.put("Model1", model1);
      expected.put("Model2", model2);

      // Call the method under test
      final Map<String, CodegenModel> actual =
          CodegenModelUtils.modelMapListToModelClassMap(modelMapList);

      // Verify the result - use specific assertion to avoid ambiguity
      assertThat(actual)
          .containsExactlyInAnyOrderEntriesOf(expected)
          .hasSize(2)
          .containsKeys("Model1", "Model2");
    }
  }
}

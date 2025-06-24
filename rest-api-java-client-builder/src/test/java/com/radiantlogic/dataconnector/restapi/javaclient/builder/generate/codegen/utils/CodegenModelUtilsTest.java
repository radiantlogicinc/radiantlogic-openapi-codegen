package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.radiantlogic.dataconnector.restapi.javaclient.builder.exceptions.ModelNotFoundException;
import java.util.List;
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
      assertFalse(CodegenModelUtils.hasDiscriminatorChildren(model));
    }

    @Test
    void itHasDiscriminatorWithNoMappings() {
      final CodegenModel model = new CodegenModel();
      model.discriminator = new CodegenDiscriminator();
      model.discriminator.setMappedModels(null);
      assertFalse(CodegenModelUtils.hasDiscriminatorChildren(model));
    }

    @Test
    void itHasDiscriminatorWithMappings() {
      final CodegenModel model = new CodegenModel();
      model.discriminator = new CodegenDiscriminator();
      model.discriminator.setMappedModels(Set.of());
      assertTrue(CodegenModelUtils.hasDiscriminatorChildren(model));
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
}

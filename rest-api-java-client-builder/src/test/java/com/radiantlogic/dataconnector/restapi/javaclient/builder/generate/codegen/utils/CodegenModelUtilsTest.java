package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenDiscriminator;
import org.openapitools.codegen.CodegenModel;

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
      throw new RuntimeException();
    }

    @Test
    void itHasTooManyModelsToExtract() {
      throw new RuntimeException();
    }

    @Test
    void itExtractsModel() {
      throw new RuntimeException();
    }
  }
}

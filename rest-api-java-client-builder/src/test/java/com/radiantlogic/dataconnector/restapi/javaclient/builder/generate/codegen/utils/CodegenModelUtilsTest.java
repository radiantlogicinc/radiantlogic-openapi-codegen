package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenDiscriminator;
import org.openapitools.codegen.CodegenModel;

public class CodegenModelUtilsTest {

  @Test
  void hasDiscriminatorChildren_WithNullDiscriminator_ReturnsFalse() {
    final CodegenModel model = new CodegenModel();
    assertFalse(CodegenModelUtils.hasDiscriminatorChildren(model));
  }

  @Test
  void hasDiscriminatorChildren_WithNullMappedModels_ReturnsFalse() {
    final CodegenModel model = new CodegenModel();
    model.discriminator = new CodegenDiscriminator();
    model.discriminator.setMappedModels(null);
    assertFalse(CodegenModelUtils.hasDiscriminatorChildren(model));
  }

  @Test
  void hasDiscriminatorChildren_WithValidDiscriminatorAndMappedModels_ReturnsTrue() {
    final CodegenModel model = new CodegenModel();
    model.discriminator = new CodegenDiscriminator();
    model.discriminator.setMappedModels(Set.of());
    assertTrue(CodegenModelUtils.hasDiscriminatorChildren(model));
  }
}

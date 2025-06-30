package com.radiantlogic.openapi.codegen.javaclient.generate.codegen.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import com.radiantlogic.openapi.codegen.javaclient.generate.codegen.utils.CodegenEnumModelUtils;
import com.radiantlogic.openapi.codegen.javaclient.generate.codegen.utils.CodegenModelUtils;
import com.radiantlogic.openapi.codegen.javaclient.generate.models.ExtendedCodegenModel;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.openapitools.codegen.CodegenDiscriminator;
import org.openapitools.codegen.CodegenDiscriminator.MappedModel;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenProperty;

public class CodegenInheritedEnumSupportTest {
  @Test
  void itFixesAndExtractsEnumsFromModelsWithParents() {
    // Arrange
    final ExtendedCodegenModel parentModel = new ExtendedCodegenModel();
    parentModel.name = "ParentModel";

    final CodegenProperty enumProperty = new CodegenProperty();
    enumProperty.baseName = "status";
    enumProperty.name = "status";
    enumProperty.dataType = "StatusEnum";
    enumProperty.datatypeWithEnum = "StatusEnum";
    enumProperty.isEnum = true;
    enumProperty.openApiType = "string";

    parentModel.vars = List.of(enumProperty);

    final ExtendedCodegenModel childModel = new ExtendedCodegenModel();
    childModel.name = "ChildModel";
    childModel.parentModel = parentModel;

    final CodegenProperty childEnumProperty = new CodegenProperty();
    childEnumProperty.baseName = "status";
    childEnumProperty.name = "status";
    childEnumProperty.dataType = "StatusEnum";
    childEnumProperty.datatypeWithEnum = "StatusEnum";
    childEnumProperty.isEnum = true;
    childEnumProperty.openApiType = "string";

    childModel.vars = List.of(childEnumProperty);

    final Map<String, CodegenModel> allModels =
        Map.ofEntries(Map.entry("ParentModel", parentModel), Map.entry("ChildModel", childModel));

    final ExtendedCodegenModel expectedEnumModel = new ExtendedCodegenModel();
    expectedEnumModel.name = "StatusEnum";
    expectedEnumModel.isEnum = true;

    // Mock the static method
    try (MockedStatic<CodegenEnumModelUtils> mockedStatic =
        mockStatic(CodegenEnumModelUtils.class)) {
      mockedStatic
          .when(() -> CodegenEnumModelUtils.createEnumModelFromEnumProp(any(CodegenProperty.class)))
          .thenReturn(expectedEnumModel);

      // Act
      final CodegenInheritedEnumSupport support = new CodegenInheritedEnumSupport();
      final ExtractedEnumModels result = support.fixAndExtractInheritedEnums(allModels);

      // Assert
      assertThat(result.enumsFromModelsWithParents()).hasSize(1);
      assertThat(result.enumsFromDiscriminatorParentModels()).isEmpty();

      assertThat(result.enumsFromModelsWithNonDiscriminatorChildren()).isEmpty();

      assertThat(result.enumsFromModelsWithParents().get(0)).isSameAs(expectedEnumModel);

      // Verify the parent property was modified correctly
      assertThat(enumProperty.isEnum).isFalse();
      assertThat(enumProperty.isInnerEnum).isFalse();
      assertThat(enumProperty.isEnumRef).isTrue();

      // Verify the child property was modified correctly
      assertThat(childEnumProperty.isEnum).isFalse();
      assertThat(childEnumProperty.isInnerEnum).isFalse();
      assertThat(childEnumProperty.isEnumRef).isTrue();
      assertThat(childEnumProperty.dataType).isEqualTo(enumProperty.dataType);
      assertThat(childEnumProperty.datatypeWithEnum).isEqualTo(enumProperty.datatypeWithEnum);
      assertThat(childEnumProperty.openApiType).isEqualTo(enumProperty.openApiType);
    }
  }

  @Test
  void itFixesAndExtractsEnumsFromAllDiscriminatedUnionModels() {
    // Arrange
    final ExtendedCodegenModel parentModel = new ExtendedCodegenModel();
    parentModel.name = "ParentModel";

    final CodegenProperty enumProperty = new CodegenProperty();
    enumProperty.baseName = "status";
    enumProperty.name = "status";
    enumProperty.dataType = "StatusEnum";
    enumProperty.datatypeWithEnum = "StatusEnum";
    enumProperty.isEnum = true;
    enumProperty.openApiType = "string";

    parentModel.vars = List.of(enumProperty);

    // Create discriminator for parent model
    final CodegenDiscriminator discriminator = new CodegenDiscriminator();
    final MappedModel mappedModel = new MappedModel("childMapping", "ChildModel", true);
    discriminator.setMappedModels(Set.of(mappedModel));
    parentModel.discriminator = discriminator;

    // Create child model
    final ExtendedCodegenModel childModel = new ExtendedCodegenModel();
    childModel.name = "ChildModel";

    final CodegenProperty childEnumProperty = new CodegenProperty();
    childEnumProperty.baseName = "status";
    childEnumProperty.name = "status";
    childEnumProperty.dataType = "StatusEnum";
    childEnumProperty.datatypeWithEnum = "StatusEnum";
    childEnumProperty.isEnum = true;
    childEnumProperty.openApiType = "string";

    childModel.vars = List.of(childEnumProperty);

    final Map<String, CodegenModel> allModels =
        Map.ofEntries(Map.entry("ParentModel", parentModel), Map.entry("ChildModel", childModel));

    // Mock static method to return expected enum model
    final ExtendedCodegenModel expectedEnumModel = new ExtendedCodegenModel();
    expectedEnumModel.name = "StatusEnum";
    expectedEnumModel.isEnum = true;

    // Mock the static method
    try (MockedStatic<CodegenEnumModelUtils> mockedStatic =
            mockStatic(CodegenEnumModelUtils.class);
        MockedStatic<CodegenModelUtils> modelUtilsMockedStatic =
            mockStatic(CodegenModelUtils.class)) {

      // Mock hasDiscriminatorChildren to return true for parent model
      modelUtilsMockedStatic
          .when(() -> CodegenModelUtils.hasDiscriminatorChildren(parentModel))
          .thenReturn(true);

      mockedStatic
          .when(() -> CodegenEnumModelUtils.createEnumModelFromEnumProp(any(CodegenProperty.class)))
          .thenReturn(expectedEnumModel);

      // Act
      final CodegenInheritedEnumSupport support = new CodegenInheritedEnumSupport();
      final ExtractedEnumModels result = support.fixAndExtractInheritedEnums(allModels);

      // Assert
      assertThat(result.enumsFromDiscriminatorParentModels()).hasSize(1);
      assertThat(result.enumsFromModelsWithParents()).isEmpty();
      assertThat(result.enumsFromModelsWithNonDiscriminatorChildren()).isEmpty();
      assertThat(result.enumsFromDiscriminatorParentModels().get(0)).isSameAs(expectedEnumModel);

      // Verify the parent property was modified correctly
      assertThat(enumProperty.isEnum).isFalse();
      assertThat(enumProperty.isInnerEnum).isFalse();
      assertThat(enumProperty.isEnumRef).isTrue();

      // Verify the child property was modified correctly
      assertThat(childEnumProperty.isEnum).isFalse();
      assertThat(childEnumProperty.isInnerEnum).isFalse();
      assertThat(childEnumProperty.isEnumRef).isTrue();
      assertThat(childEnumProperty.dataType).isEqualTo(enumProperty.dataType);
      assertThat(childEnumProperty.datatypeWithEnum).isEqualTo(enumProperty.datatypeWithEnum);
      assertThat(childEnumProperty.openApiType).isEqualTo(enumProperty.openApiType);
    }
  }

  @Test
  void itFixesAndExtractsEnumsFromNonDiscriminatorModelsWithChildren() {
    // Arrange
    final ExtendedCodegenModel parentModel = new ExtendedCodegenModel();
    parentModel.name = "ParentModel";

    final CodegenProperty enumProperty = new CodegenProperty();
    enumProperty.baseName = "status";
    enumProperty.name = "status";
    enumProperty.dataType = "StatusEnum";
    enumProperty.datatypeWithEnum = "StatusEnum";
    enumProperty.isEnum = true;
    enumProperty.openApiType = "string";

    parentModel.vars = List.of(enumProperty);

    parentModel.oneOf = Set.of("ChildModel");

    final ExtendedCodegenModel childModel = new ExtendedCodegenModel();
    childModel.name = "ChildModel";

    final CodegenProperty childEnumProperty = new CodegenProperty();
    childEnumProperty.baseName = "status";
    childEnumProperty.name = "status";
    childEnumProperty.dataType = "StatusEnum";
    childEnumProperty.datatypeWithEnum = "StatusEnum";
    childEnumProperty.isEnum = true;
    childEnumProperty.openApiType = "string";

    childModel.vars = List.of(childEnumProperty);

    final Map<String, CodegenModel> allModels =
        Map.ofEntries(Map.entry("ParentModel", parentModel), Map.entry("ChildModel", childModel));

    final ExtendedCodegenModel expectedEnumModel = new ExtendedCodegenModel();
    expectedEnumModel.name = "StatusEnum";
    expectedEnumModel.isEnum = true;

    // Mock the static methods
    try (MockedStatic<CodegenEnumModelUtils> mockedStatic =
        mockStatic(CodegenEnumModelUtils.class)) {

      mockedStatic
          .when(() -> CodegenEnumModelUtils.createEnumModelFromEnumProp(any(CodegenProperty.class)))
          .thenReturn(expectedEnumModel);

      // Act
      final CodegenInheritedEnumSupport support = new CodegenInheritedEnumSupport();
      final ExtractedEnumModels result = support.fixAndExtractInheritedEnums(allModels);

      // Assert
      assertThat(result.enumsFromModelsWithNonDiscriminatorChildren()).hasSize(1);
      assertThat(result.enumsFromModelsWithParents()).isEmpty();
      assertThat(result.enumsFromDiscriminatorParentModels()).isEmpty();
      assertThat(result.enumsFromModelsWithNonDiscriminatorChildren().get(0))
          .isSameAs(expectedEnumModel);

      // Verify the parent property was modified correctly
      assertThat(enumProperty.isEnum).isFalse();
      assertThat(enumProperty.isInnerEnum).isFalse();
      assertThat(enumProperty.isEnumRef).isTrue();

      // Verify the child property was modified correctly
      assertThat(childEnumProperty.isEnum).isFalse();
      assertThat(childEnumProperty.isInnerEnum).isFalse();
      assertThat(childEnumProperty.isEnumRef).isTrue();
      assertThat(childEnumProperty.dataType).isEqualTo(enumProperty.dataType);
      assertThat(childEnumProperty.datatypeWithEnum).isEqualTo(enumProperty.datatypeWithEnum);
      assertThat(childEnumProperty.openApiType).isEqualTo(enumProperty.openApiType);
    }
  }
}

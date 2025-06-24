package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenDiscriminator;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenProperty;

public class CodegenDiscriminatorSupportTest {
  private final CodegenDiscriminatorSupport codegenDiscriminatorSupport =
      new CodegenDiscriminatorSupport();

  @Test
  void itDoesNotChangeTypeWhenNoDiscriminator() {
    final CodegenModel model = new CodegenModel();
    model.setDiscriminator(null);

    codegenDiscriminatorSupport.fixDiscriminatorType(model);

    assertThat(model.getDiscriminator()).isNull();
  }

  @Test
  void itDoesNotChangeTypeWhenCorrectType() {
    final CodegenModel model = new CodegenModel();
    final CodegenDiscriminator discriminator = new CodegenDiscriminator();
    final String propertyName = "type";
    discriminator.setPropertyBaseName(propertyName);
    discriminator.setPropertyType("MyType");
    model.setDiscriminator(discriminator);

    final CodegenProperty property = new CodegenProperty();
    property.setBaseName(propertyName);
    property.setDatatypeWithEnum("MyType");
    model.setVars(new ArrayList<>(List.of(property)));

    codegenDiscriminatorSupport.fixDiscriminatorType(model);

    assertThat(model.getDiscriminator().getPropertyType()).isEqualTo("MyType");
  }

  @Test
  void itChangesTypeWhenIncorrect() {
    final CodegenModel model = new CodegenModel();
    final CodegenDiscriminator discriminator = new CodegenDiscriminator();
    final String propertyName = "type";
    discriminator.setPropertyBaseName(propertyName);
    discriminator.setPropertyType("String");
    model.setDiscriminator(discriminator);

    final CodegenProperty property = new CodegenProperty();
    property.setBaseName(propertyName);
    property.setDatatypeWithEnum("MyType");
    model.setVars(new ArrayList<>(List.of(property)));

    codegenDiscriminatorSupport.fixDiscriminatorType(model);

    assertThat(model.getDiscriminator().getPropertyType()).isEqualTo("MyType");
  }

  @Test
  void itAssignsVendorExtensionToDiscriminatorMappings() {
    // Arrange
    final CodegenModel parentModel = new CodegenModel();
    parentModel.classname = "ParentModel";

    final CodegenDiscriminator discriminator = new CodegenDiscriminator();
    discriminator.setPropertyBaseName("type");
    parentModel.setDiscriminator(discriminator);

    final CodegenModel childModel1 = new CodegenModel();
    childModel1.classname = "ChildModel1";
    childModel1.name = childModel1.classname;

    final CodegenModel childModel2 = new CodegenModel();
    childModel2.classname = "ChildModel2";
    childModel2.name = childModel2.classname;

    discriminator.setMappedModels(
        Set.of(
            new CodegenDiscriminator.MappedModel("one", childModel1.name, true),
            new CodegenDiscriminator.MappedModel("two", childModel2.name, true)));

    final Map<String, CodegenModel> allModels =
        Map.of(
            parentModel.classname, parentModel,
            childModel1.classname, childModel1,
            childModel2.classname, childModel2);

    // Act
    codegenDiscriminatorSupport.fixAllDiscriminatorMappings(allModels);

    // Assert
    assertThat(childModel1.vendorExtensions).containsEntry("x-discriminator-mapping-value", "one");
    assertThat(childModel2.vendorExtensions).containsEntry("x-discriminator-mapping-value", "two");
  }
}

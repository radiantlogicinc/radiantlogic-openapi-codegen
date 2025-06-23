package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenDiscriminator;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenProperty;

public class CodegenDiscriminatorTypeSupportTest {
  private final CodegenDiscriminatorTypeSupport codegenDiscriminatorTypeSupport =
      new CodegenDiscriminatorTypeSupport();

  @Test
  void testItHasNoDiscriminator() {
    // Create a model with no discriminator
    final CodegenModel model = new CodegenModel();
    model.setDiscriminator(null);

    // Call the method under test
    codegenDiscriminatorTypeSupport.fixDiscriminatorType(model);

    // Verify the model still has no discriminator
    assertThat(model.getDiscriminator()).isNull();
  }

  @Test
  void testItHasDiscriminatorWithCorrectType() {
    // Create a model with a discriminator
    final CodegenModel model = new CodegenModel();
    final CodegenDiscriminator discriminator = new CodegenDiscriminator();
    final String propertyName = "type";
    discriminator.setPropertyBaseName(propertyName);
    discriminator.setPropertyType("String");
    model.setDiscriminator(discriminator);

    // Create a property that matches the discriminator property name
    final CodegenProperty property = new CodegenProperty();
    property.setBaseName(propertyName);
    property.setDatatypeWithEnum("String");

    // Add the property to the model
    model.setVars(new ArrayList<>(List.of(property)));

    // Call the method under test
    codegenDiscriminatorTypeSupport.fixDiscriminatorType(model);

    // Verify the discriminator property type is still "String"
    assertThat(model.getDiscriminator().getPropertyType()).isEqualTo("String");
  }

  @Test
  void testItHasDiscriminatorWithIncorrectType() {
    // Create a model with a discriminator that has an incorrect type
    final CodegenModel model = new CodegenModel();
    final CodegenDiscriminator discriminator = new CodegenDiscriminator();
    final String propertyName = "type";
    discriminator.setPropertyBaseName(propertyName);
    discriminator.setPropertyType("Object"); // Incorrect type
    model.setDiscriminator(discriminator);

    // Create a property that matches the discriminator property name
    final CodegenProperty property = new CodegenProperty();
    property.setBaseName(propertyName);
    property.setDatatypeWithEnum("String"); // Correct type

    // Add the property to the model
    model.setVars(new ArrayList<>(List.of(property)));

    // Call the method under test
    codegenDiscriminatorTypeSupport.fixDiscriminatorType(model);

    // Verify the discriminator property type has been updated to match the property's type
    assertThat(model.getDiscriminator().getPropertyType()).isEqualTo("String");
  }
}

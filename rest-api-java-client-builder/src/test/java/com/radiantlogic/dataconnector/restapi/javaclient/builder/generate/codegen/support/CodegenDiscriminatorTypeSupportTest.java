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
    final CodegenModel model = new CodegenModel();
    model.setDiscriminator(null);
    codegenDiscriminatorTypeSupport.fixDiscriminatorType(model);
    assertThat(model.getDiscriminator()).isNull();
  }

  @Test
  void testItHasDiscriminatorWithCorrectType() {
    final CodegenModel model = new CodegenModel();
    final CodegenDiscriminator discriminator = new CodegenDiscriminator();
    final String propertyName = "type";
    discriminator.setPropertyBaseName(propertyName);
    discriminator.setPropertyType("String");
    model.setDiscriminator(discriminator);
    final CodegenProperty property = new CodegenProperty();
    property.setBaseName(propertyName);
    property.setDatatypeWithEnum("String");
    model.setVars(new ArrayList<>(List.of(property)));
    codegenDiscriminatorTypeSupport.fixDiscriminatorType(model);
    assertThat(model.getDiscriminator().getPropertyType()).isEqualTo("String");
  }

  @Test
  void testItHasDiscriminatorWithIncorrectType() {
    final CodegenModel model = new CodegenModel();
    final CodegenDiscriminator discriminator = new CodegenDiscriminator();
    final String propertyName = "type";
    discriminator.setPropertyBaseName(propertyName);
    discriminator.setPropertyType("Object");
    model.setDiscriminator(discriminator);
    final CodegenProperty property = new CodegenProperty();
    property.setBaseName(propertyName);
    property.setDatatypeWithEnum("String");
    model.setVars(new ArrayList<>(List.of(property)));
    codegenDiscriminatorTypeSupport.fixDiscriminatorType(model);
    assertThat(model.getDiscriminator().getPropertyType()).isEqualTo("String");
  }
}

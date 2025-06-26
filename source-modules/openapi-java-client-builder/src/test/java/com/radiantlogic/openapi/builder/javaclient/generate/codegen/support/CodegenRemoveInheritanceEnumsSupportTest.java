package com.radiantlogic.openapi.builder.javaclient.generate.codegen.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenProperty;

public class CodegenRemoveInheritanceEnumsSupportTest {
  private final CodegenRemoveInheritanceEnumsSupport codegenRemoveInheritanceEnumsSupport =
      new CodegenRemoveInheritanceEnumsSupport();

  @Test
  void itDoesNothingIfNoParent() {
    // Arrange
    final CodegenModel model = new CodegenModel();
    model.classname = "ChildModel";
    model.vars = new ArrayList<>();

    final CodegenProperty enumProperty = new CodegenProperty();
    enumProperty.name = "status";
    enumProperty.isEnum = true;
    enumProperty.dataType = "StatusEnum";
    enumProperty.datatypeWithEnum = "StatusEnum";
    enumProperty.openApiType = "string";
    model.vars.add(enumProperty);

    final Map<String, CodegenModel> allModels = new HashMap<>();
    allModels.put("ChildModel", model);

    // Act
    codegenRemoveInheritanceEnumsSupport.removeInheritedEnums(allModels);

    // Assert
    assertThat(enumProperty.isEnum).isTrue();
    assertThat(enumProperty.dataType).isEqualTo("StatusEnum");
    assertThat(enumProperty.datatypeWithEnum).isEqualTo("StatusEnum");
    assertThat(enumProperty.openApiType).isEqualTo("string");
  }

  @Test
  void itDoesNothingIfParentHasSameEnum() {
    // Arrange
    final CodegenModel parentModel = new CodegenModel();
    parentModel.classname = "ParentModel";
    parentModel.vars = new ArrayList<>();

    final CodegenProperty parentEnumProperty = new CodegenProperty();
    parentEnumProperty.name = "status";
    parentEnumProperty.isEnum = true;
    parentEnumProperty.dataType = "StatusEnum";
    parentEnumProperty.datatypeWithEnum = "StatusEnum";
    parentEnumProperty.openApiType = "string";
    parentModel.vars.add(parentEnumProperty);

    final CodegenModel childModel = new CodegenModel();
    childModel.classname = "ChildModel";
    childModel.parentModel = parentModel;
    childModel.vars = new ArrayList<>();

    final CodegenProperty childEnumProperty = new CodegenProperty();
    childEnumProperty.name = "status";
    childEnumProperty.isEnum = true;
    childEnumProperty.dataType = "StatusEnum";
    childEnumProperty.datatypeWithEnum = "StatusEnum";
    childEnumProperty.openApiType = "string";
    childModel.vars.add(childEnumProperty);

    final Map<String, CodegenModel> allModels = new HashMap<>();
    allModels.put("ParentModel", parentModel);
    allModels.put("ChildModel", childModel);

    // Act
    codegenRemoveInheritanceEnumsSupport.removeInheritedEnums(allModels);

    // Assert
    assertThat(childEnumProperty.isEnum).isTrue();
    assertThat(childEnumProperty.dataType).isEqualTo("StatusEnum");
    assertThat(childEnumProperty.datatypeWithEnum).isEqualTo("StatusEnum");
    assertThat(childEnumProperty.openApiType).isEqualTo("string");
  }

  @Test
  void itRemovesChildEnumIfParentNotEnum() {
    // Arrange
    final CodegenModel parentModel = new CodegenModel();
    parentModel.classname = "ParentModel";
    parentModel.vars = new ArrayList<>();

    final CodegenProperty parentProperty = new CodegenProperty();
    parentProperty.name = "status";
    parentProperty.isEnum = false;
    parentProperty.dataType = "String";
    parentProperty.datatypeWithEnum = "String";
    parentProperty.openApiType = "string";
    parentModel.vars.add(parentProperty);

    final CodegenModel childModel = new CodegenModel();
    childModel.classname = "ChildModel";
    childModel.parentModel = parentModel;
    childModel.vars = new ArrayList<>();

    final CodegenProperty childEnumProperty = new CodegenProperty();
    childEnumProperty.name = "status";
    childEnumProperty.isEnum = true;
    childEnumProperty.dataType = "StatusEnum";
    childEnumProperty.datatypeWithEnum = "StatusEnum";
    childEnumProperty.openApiType = "string";
    childModel.vars.add(childEnumProperty);

    final Map<String, CodegenModel> allModels = new HashMap<>();
    allModels.put("ParentModel", parentModel);
    allModels.put("ChildModel", childModel);

    // Act
    codegenRemoveInheritanceEnumsSupport.removeInheritedEnums(allModels);

    // Assert
    assertThat(childEnumProperty.isEnum).isFalse();
    assertThat(childEnumProperty.dataType).isEqualTo("String");
    assertThat(childEnumProperty.datatypeWithEnum).isEqualTo("String");
    assertThat(childEnumProperty.openApiType).isEqualTo("string");
  }

  @Test
  void itHandlesMultiLevelInheritance() {
    // Arrange
    final CodegenModel grandparentModel = new CodegenModel();
    grandparentModel.classname = "GrandparentModel";
    grandparentModel.vars = new ArrayList<>();

    final CodegenProperty grandparentProperty = new CodegenProperty();
    grandparentProperty.name = "status";
    grandparentProperty.isEnum = false;
    grandparentProperty.dataType = "String";
    grandparentProperty.datatypeWithEnum = "String";
    grandparentProperty.openApiType = "string";
    grandparentModel.vars.add(grandparentProperty);

    final CodegenModel parentModel = new CodegenModel();
    parentModel.classname = "ParentModel";
    parentModel.parentModel = grandparentModel;
    parentModel.vars = new ArrayList<>();

    final CodegenProperty parentEnumProperty = new CodegenProperty();
    parentEnumProperty.name = "status";
    parentEnumProperty.isEnum = true;
    parentEnumProperty.dataType = "StatusEnum";
    parentEnumProperty.datatypeWithEnum = "StatusEnum";
    parentEnumProperty.openApiType = "string";
    parentModel.vars.add(parentEnumProperty);

    final CodegenModel childModel = new CodegenModel();
    childModel.classname = "ChildModel";
    childModel.parentModel = parentModel;
    childModel.vars = new ArrayList<>();

    final CodegenProperty childEnumProperty = new CodegenProperty();
    childEnumProperty.name = "status";
    childEnumProperty.isEnum = true;
    childEnumProperty.dataType = "StatusEnum";
    childEnumProperty.datatypeWithEnum = "StatusEnum";
    childEnumProperty.openApiType = "string";
    childModel.vars.add(childEnumProperty);

    final Map<String, CodegenModel> allModels = new HashMap<>();
    allModels.put("GrandparentModel", grandparentModel);
    allModels.put("ParentModel", parentModel);
    allModels.put("ChildModel", childModel);

    // Act
    codegenRemoveInheritanceEnumsSupport.removeInheritedEnums(allModels);

    // Assert
    // Both parent and child enums should be removed since grandparent has non-enum property
    assertThat(parentEnumProperty.isEnum).isFalse();
    assertThat(parentEnumProperty.dataType).isEqualTo("String");
    assertThat(childEnumProperty.isEnum).isFalse();
    assertThat(childEnumProperty.dataType).isEqualTo("String");
  }

  @Test
  void itHandlesMultiplePropertiesCorrectly() {
    // Arrange
    final CodegenModel parentModel = new CodegenModel();
    parentModel.classname = "ParentModel";
    parentModel.vars = new ArrayList<>();

    final CodegenProperty parentEnumProperty = new CodegenProperty();
    parentEnumProperty.name = "status";
    parentEnumProperty.isEnum = true;
    parentEnumProperty.dataType = "StatusEnum";
    parentEnumProperty.datatypeWithEnum = "StatusEnum";
    parentEnumProperty.openApiType = "string";
    parentModel.vars.add(parentEnumProperty);

    final CodegenProperty parentNonEnumProperty = new CodegenProperty();
    parentNonEnumProperty.name = "type";
    parentNonEnumProperty.isEnum = false;
    parentNonEnumProperty.dataType = "String";
    parentNonEnumProperty.datatypeWithEnum = "String";
    parentNonEnumProperty.openApiType = "string";
    parentModel.vars.add(parentNonEnumProperty);

    final CodegenModel childModel = new CodegenModel();
    childModel.classname = "ChildModel";
    childModel.parentModel = parentModel;
    childModel.vars = new ArrayList<>();

    final CodegenProperty childEnumProperty1 = new CodegenProperty();
    childEnumProperty1.name = "status";
    childEnumProperty1.isEnum = true;
    childEnumProperty1.dataType = "StatusEnum";
    childEnumProperty1.datatypeWithEnum = "StatusEnum";
    childEnumProperty1.openApiType = "string";
    childModel.vars.add(childEnumProperty1);

    final CodegenProperty childEnumProperty2 = new CodegenProperty();
    childEnumProperty2.name = "type";
    childEnumProperty2.isEnum = true;
    childEnumProperty2.dataType = "TypeEnum";
    childEnumProperty2.datatypeWithEnum = "TypeEnum";
    childEnumProperty2.openApiType = "string";
    childModel.vars.add(childEnumProperty2);

    final Map<String, CodegenModel> allModels = new HashMap<>();
    allModels.put("ParentModel", parentModel);
    allModels.put("ChildModel", childModel);

    // Act
    codegenRemoveInheritanceEnumsSupport.removeInheritedEnums(allModels);

    // Assert
    // First property should remain enum since parent also has enum
    assertThat(childEnumProperty1.isEnum).isTrue();
    assertThat(childEnumProperty1.dataType).isEqualTo("StatusEnum");

    // Second property should be converted since parent has non-enum
    assertThat(childEnumProperty2.isEnum).isFalse();
    assertThat(childEnumProperty2.dataType).isEqualTo("String");
  }
}

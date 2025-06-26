package com.radiantlogic.openapi.codegen.javaclient.generate.codegen.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.radiantlogic.openapi.codegen.javaclient.exceptions.ModelNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenModel;

public class CodegenMissingModelInheritanceSupportTest {
  private final CodegenMissingModelInheritanceSupport codegenMissingModelInheritanceSupport =
      new CodegenMissingModelInheritanceSupport();

  @Test
  void itDoesNothingForModelWithParent() {
    // Arrange
    final CodegenModel model = new CodegenModel();
    model.classname = "ChildModel";
    model.parent = "ParentModel";

    final Map<String, CodegenModel> allModels = new HashMap<>();
    allModels.put("ChildModel", model);

    // Act
    codegenMissingModelInheritanceSupport.fixInheritanceAllModels(allModels);

    // Assert
    assertThat(model.parent).isEqualTo("ParentModel");
  }

  @Test
  void itDoesNothingForModelWithNoDatatype() {
    // Arrange
    final CodegenModel model = new CodegenModel();
    model.classname = "MyModel";
    model.dataType = null;

    final Map<String, CodegenModel> allModels = new HashMap<>();
    allModels.put("MyModel", model);

    // Act
    codegenMissingModelInheritanceSupport.fixInheritanceAllModels(allModels);

    // Assert
    assertThat(model.parent).isNull();
  }

  @Test
  void itDoesNothingForModelWithDatatypeEqualToClassname() {
    // Arrange
    final CodegenModel model = new CodegenModel();
    model.classname = "MyModel";
    model.dataType = "MyModel";

    final Map<String, CodegenModel> allModels = new HashMap<>();
    allModels.put("MyModel", model);

    // Act
    codegenMissingModelInheritanceSupport.fixInheritanceAllModels(allModels);

    // Assert
    assertThat(model.parent).isNull();
  }

  @Test
  void itDoesNothingForModelThatIsEnum() {
    // Arrange
    final CodegenModel model = new CodegenModel();
    model.classname = "MyModel";
    model.isEnum = true;
    model.dataType = "SomeType";

    final Map<String, CodegenModel> allModels = new HashMap<>();
    allModels.put("MyEnum", model);

    // Act
    codegenMissingModelInheritanceSupport.fixInheritanceAllModels(allModels);

    // Assert
    assertThat(model.parent).isNull();
  }

  @Test
  void itDoesNothingForModelWithNoInterfaces() {
    // Arrange
    final CodegenModel model = new CodegenModel();
    model.classname = "MyModel";
    model.dataType = "SomeType";
    model.interfaces = null;

    final Map<String, CodegenModel> allModels = new HashMap<>();
    allModels.put("MyModel", model);

    // Act
    codegenMissingModelInheritanceSupport.fixInheritanceAllModels(allModels);

    // Assert
    assertThat(model.parent).isNull();
  }

  @Test
  void itDoesNothingForModelWithNoAllOf() {
    // Arrange
    final CodegenModel model = new CodegenModel();
    model.classname = "MyModel";
    model.dataType = "SomeType";
    model.interfaces = new ArrayList<>(List.of("Interface1"));
    model.allOf = null;

    final Map<String, CodegenModel> allModels = new HashMap<>();
    allModels.put("MyModel", model);

    // Act
    codegenMissingModelInheritanceSupport.fixInheritanceAllModels(allModels);

    // Assert
    assertThat(model.parent).isNull();
  }

  @Test
  void itDoesNothingForModelWithMultipleInterfaces() {
    // Arrange
    final CodegenModel model = new CodegenModel();
    model.classname = "MyModel";
    model.dataType = "SomeType";
    model.interfaces = new ArrayList<>(List.of("Interface1", "Interface2"));
    model.allOf = new HashSet<>(Set.of("AllOf1"));

    final Map<String, CodegenModel> allModels = new HashMap<>();
    allModels.put("MyModel", model);

    // Act
    codegenMissingModelInheritanceSupport.fixInheritanceAllModels(allModels);

    // Assert
    assertThat(model.parent).isNull();
  }

  @Test
  void itDoesNothingForModelWithMultipleAllOf() {
    // Arrange
    final CodegenModel model = new CodegenModel();
    model.classname = "MyModel";
    model.dataType = "SomeType";
    model.interfaces = new ArrayList<>(List.of("Interface1"));
    model.allOf = new HashSet<>(Set.of("AllOf1", "AllOf2"));

    final Map<String, CodegenModel> allModels = new HashMap<>();
    allModels.put("MyModel", model);

    // Act
    codegenMissingModelInheritanceSupport.fixInheritanceAllModels(allModels);

    // Assert
    assertThat(model.parent).isNull();
  }

  @Test
  void itDoesNothingIfModelInterfaceDoesNotEqualModelAllOf() {
    // Arrange
    final CodegenModel model = new CodegenModel();
    model.classname = "MyModel";
    model.dataType = "SomeType";
    model.interfaces = new ArrayList<>(List.of("Interface1"));
    model.allOf = new HashSet<>(Set.of("AllOf1"));

    final Map<String, CodegenModel> allModels = new HashMap<>();
    allModels.put("MyModel", model);

    // Act
    codegenMissingModelInheritanceSupport.fixInheritanceAllModels(allModels);

    // Assert
    assertThat(model.parent).isNull();
  }

  @Test
  void itDoesNothingIfModelInterfaceDoesNotEqualDataType() {
    // Arrange
    final CodegenModel model = new CodegenModel();
    model.classname = "MyModel";
    model.dataType = "SomeType";
    model.interfaces = new ArrayList<>(List.of("Interface1"));
    model.allOf = new HashSet<>(Set.of("Interface1"));

    final Map<String, CodegenModel> allModels = new HashMap<>();
    allModels.put("MyModel", model);

    // Act
    codegenMissingModelInheritanceSupport.fixInheritanceAllModels(allModels);

    // Assert
    assertThat(model.parent).isNull();
  }

  @Test
  void itThrowsExceptionIfParentModelNotFound() {
    // Arrange
    final CodegenModel model = new CodegenModel();
    model.classname = "MyModel";
    model.dataType = "ParentType";
    model.interfaces = new ArrayList<>(List.of("ParentType"));
    model.allOf = new HashSet<>(Set.of("ParentType"));

    final Map<String, CodegenModel> allModels = new HashMap<>();
    allModels.put("MyModel", model);

    // Act & Assert
    assertThatThrownBy(
            () -> codegenMissingModelInheritanceSupport.fixInheritanceAllModels(allModels))
        .isInstanceOf(ModelNotFoundException.class)
        .hasMessageContaining("Parent model should exist but was not found: ParentType");
  }

  @Test
  void itFixesMissingInheritance() {
    // Arrange
    final CodegenModel model = new CodegenModel();
    model.classname = "MyModel";
    model.dataType = "ParentType";
    model.interfaces = new ArrayList<>(List.of("ParentType"));
    model.allOf = new HashSet<>(Set.of("ParentType"));

    final CodegenModel parentModel = new CodegenModel();
    parentModel.classname = "ParentType";

    final Map<String, CodegenModel> allModels = new HashMap<>();
    allModels.put("MyModel", model);
    allModels.put("ParentType", parentModel);

    // Act
    codegenMissingModelInheritanceSupport.fixInheritanceAllModels(allModels);

    // Assert
    assertThat(model.parent).isEqualTo("ParentType");
    assertThat(model.parentModel).isSameAs(parentModel);
  }
}

package com.radiantlogic.openapi.codegen.javaclient.generate.codegen.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.radiantlogic.openapi.codegen.javaclient.generate.models.ExtendedCodegenModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.CodegenParameter;
import org.openapitools.codegen.CodegenProperty;

public class CodegenRawTypeUsageSupportTest {
  @Test
  void itAppliesRawTypesToModelProperties() {
    // Given
    // Create an invalid union type (oneOf but no discriminator)
    final ExtendedCodegenModel invalidUnionModel = new ExtendedCodegenModel();
    invalidUnionModel.classname = "InvalidUnion";
    invalidUnionModel.oneOf = Set.of("Child1", "Child2");

    // Create a model with a property that references the invalid union type
    final CodegenModel modelWithInvalidUnionProperty = new CodegenModel();
    modelWithInvalidUnionProperty.classname = "ModelWithInvalidUnionProperty";

    // Create a property that references the invalid union type
    final CodegenProperty property = new CodegenProperty();
    property.complexType = "InvalidUnion";
    property.datatypeWithEnum = "InvalidUnion";

    // Create a property with a list of the invalid union type
    final CodegenProperty listProperty = new CodegenProperty();
    listProperty.complexType = "InvalidUnion";
    listProperty.datatypeWithEnum = "List<InvalidUnion>";
    listProperty.items = new CodegenProperty();
    listProperty.items.complexType = "InvalidUnion";
    listProperty.items.datatypeWithEnum = "InvalidUnion";

    modelWithInvalidUnionProperty.vars = List.of(property, listProperty);

    // Create a map of model classes
    final Map<String, CodegenModel> modelClassMap = new HashMap<>();
    modelClassMap.put("InvalidUnion", invalidUnionModel);
    modelClassMap.put("ModelWithInvalidUnionProperty", modelWithInvalidUnionProperty);

    // When
    final CodegenRawTypeUsageSupport support = new CodegenRawTypeUsageSupport();
    support.applyRawTypesToModelProperties(modelClassMap);

    // Then
    // Verify that the property's type has been changed to use the Raw type
    assertThat(property.complexType).isEqualTo("InvalidUnion.Raw");
    assertThat(property.datatypeWithEnum).isEqualTo("InvalidUnion.Raw");

    // Verify that the list property's type has been changed to use the Raw type
    assertThat(listProperty.complexType).isEqualTo("InvalidUnion.Raw");
    assertThat(listProperty.datatypeWithEnum).isEqualTo("List<InvalidUnion.Raw>");
    assertThat(listProperty.items.complexType).isEqualTo("InvalidUnion.Raw");
    assertThat(listProperty.items.datatypeWithEnum).isEqualTo("InvalidUnion.Raw");
  }

  @Test
  void itAppliesRawTypesToOperationTypes() {
    // Given
    // Create an invalid union type (oneOf but no discriminator)
    final ExtendedCodegenModel invalidUnionModel = new ExtendedCodegenModel();
    invalidUnionModel.classname = "InvalidUnion";
    invalidUnionModel.oneOf = Set.of("Child1", "Child2");

    // Create a map of model classes
    final Map<String, CodegenModel> modelClassMap = new HashMap<>();
    modelClassMap.put("InvalidUnion", invalidUnionModel);

    // Create an operation with a return type that references the invalid union type
    final CodegenOperation operation = new CodegenOperation();
    operation.returnBaseType = "InvalidUnion";
    operation.returnType = "InvalidUnion";

    // Create an operation with a list return type that references the invalid union type
    final CodegenOperation listOperation = new CodegenOperation();
    listOperation.returnBaseType = "InvalidUnion";
    listOperation.returnType = "List<InvalidUnion>";

    // Create an operation with a parameter that references the invalid union type
    final CodegenParameter parameter = new CodegenParameter();
    parameter.baseType = "InvalidUnion";
    parameter.dataType = "InvalidUnion";

    // Create an operation with a list parameter that references the invalid union type
    final CodegenParameter listParameter = new CodegenParameter();
    listParameter.baseType = "InvalidUnion";
    listParameter.dataType = "List<InvalidUnion>";

    final CodegenOperation paramOperation = new CodegenOperation();
    paramOperation.allParams = List.of(parameter, listParameter);

    final List<CodegenOperation> operations = List.of(operation, listOperation, paramOperation);

    // When
    final CodegenRawTypeUsageSupport support = new CodegenRawTypeUsageSupport();
    support.applyRawTypesToOperationTypes(operations, modelClassMap);

    // Then
    // Verify that the operation's return type has been changed to use the Raw type
    assertThat(operation.returnBaseType).isEqualTo("InvalidUnion.Raw");
    assertThat(operation.returnType).isEqualTo("InvalidUnion.Raw");

    // Verify that the list operation's return type has been changed to use the Raw type
    assertThat(listOperation.returnBaseType).isEqualTo("InvalidUnion.Raw");
    assertThat(listOperation.returnType).isEqualTo("List<InvalidUnion.Raw>");

    // Verify that the parameter's type has been changed to use the Raw type
    assertThat(parameter.baseType).isEqualTo("InvalidUnion.Raw");
    assertThat(parameter.dataType).isEqualTo("InvalidUnion.Raw");

    // Verify that the list parameter's type has been changed to use the Raw type
    assertThat(listParameter.baseType).isEqualTo("InvalidUnion.Raw");
    assertThat(listParameter.dataType).isEqualTo("List<InvalidUnion.Raw>");
  }
}

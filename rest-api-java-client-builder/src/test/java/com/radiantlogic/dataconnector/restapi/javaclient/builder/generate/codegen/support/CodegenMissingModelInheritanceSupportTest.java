package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support;

import org.junit.jupiter.api.Test;

public class CodegenMissingModelInheritanceSupportTest {
  @Test
  void itDoesNothingForModelWithParent() {
    throw new RuntimeException();
  }

  @Test
  void itDoesNothingForModelWithNoDatatype() {
    throw new RuntimeException();
  }

  @Test
  void itDoesNothingForModelWithDatatypeEqualToClassname() {
    throw new RuntimeException();
  }

  @Test
  void itDoesNothingForModelThatIsEnum() {
    throw new RuntimeException();
  }

  @Test
  void itDoesNothingForModelWithNoInterfaces() {
    throw new RuntimeException();
  }

  @Test
  void itDoesNothingForModelWithNoAllOf() {
    throw new RuntimeException();
  }

  @Test
  void itDoesNothingForModelWithMultipleInterfaces() {
    throw new RuntimeException();
  }

  @Test
  void itDoesNothingForModelWithMultipleAllOf() {
    throw new RuntimeException();
  }

  @Test
  void itDoesNothingIfModelInterfaceDoesNotEqualModelAllOf() {
    throw new RuntimeException();
  }

  @Test
  void itDoesNothingIfModelInterfaceDoesNotEqualDataType() {
    throw new RuntimeException();
  }

  @Test
  void itThrowsExceptionIfParentModelNotFound() {
    throw new RuntimeException();
  }

  @Test
  void itFixesMissingInheritance() {
    throw new RuntimeException();
  }
}

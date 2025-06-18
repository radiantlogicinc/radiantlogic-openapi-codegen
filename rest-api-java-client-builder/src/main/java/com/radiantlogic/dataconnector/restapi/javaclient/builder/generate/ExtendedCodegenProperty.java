package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.openapitools.codegen.CodegenProperty;

@RequiredArgsConstructor
public class ExtendedCodegenProperty extends CodegenProperty {
  @Delegate(types = CodegenProperty.class)
  private final CodegenProperty delegate;

  public static void main(final String[] args) {
    final CodegenProperty prop = new CodegenProperty();
    prop.name = "HelloWorld";
    final ExtendedCodegenProperty extendedProp = new ExtendedCodegenProperty(prop);
    System.out.println(extendedProp.name);
  }
}

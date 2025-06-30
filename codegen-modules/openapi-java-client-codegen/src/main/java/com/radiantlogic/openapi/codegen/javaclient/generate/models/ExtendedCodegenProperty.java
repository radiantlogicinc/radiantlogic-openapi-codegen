package com.radiantlogic.openapi.codegen.javaclient.generate.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.codegen.CodegenProperty;

/** An extended version of the CodegenProperty class adding more functionality. */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class ExtendedCodegenProperty extends CodegenProperty {
  private String jsonName;
}

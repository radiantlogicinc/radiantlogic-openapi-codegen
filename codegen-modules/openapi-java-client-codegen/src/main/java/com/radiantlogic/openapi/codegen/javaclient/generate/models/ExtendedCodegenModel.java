package com.radiantlogic.openapi.codegen.javaclient.generate.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.codegen.CodegenModel;

/** An extended version of the CodegenModel class adding more functionality. */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class ExtendedCodegenModel extends CodegenModel {
  private String equalsClassVarName;
}

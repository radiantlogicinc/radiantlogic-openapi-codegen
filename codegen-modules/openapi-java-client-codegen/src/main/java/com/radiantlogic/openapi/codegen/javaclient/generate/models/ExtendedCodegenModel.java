package com.radiantlogic.openapi.codegen.javaclient.generate.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.codegen.CodegenModel;

/**
 * An extended version of the CodegenModel class adding more functionality. Instance variables are
 * public because that's how CodegenModel works, and it is possible (although not tested and
 * confirmed) that this is a requirement of exposing these properties to the mustache engine.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class ExtendedCodegenModel extends CodegenModel {
  public String equalsClassVarName;
}

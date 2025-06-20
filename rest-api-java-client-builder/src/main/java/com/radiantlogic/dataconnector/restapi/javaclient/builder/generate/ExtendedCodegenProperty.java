package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.codegen.CodegenProperty;

/**
 * An extended version of the CodegenProperty class adding more functionality. Instance variables
 * are public because that's how CodegenProperty works, and it is possible (although not tested and
 * confirmed) that this is a requirement of exposing these properties to the mustache engine.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class ExtendedCodegenProperty extends CodegenProperty {
  public String jsonName;
}

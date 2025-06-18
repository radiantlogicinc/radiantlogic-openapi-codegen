package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.codegen.CodegenProperty;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class ExtendedCodegenProperty extends CodegenProperty {
  public String jsonName;
}

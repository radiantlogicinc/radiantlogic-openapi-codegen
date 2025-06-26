package com.radiantlogic.openapi.codegen.javaclient.generate.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.codegen.CodegenDiscriminator;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class ExtendedCodegenDiscriminator extends CodegenDiscriminator {
  // TODO will this work in model
  public boolean hasNoMapping() {
    return true;
  }
}

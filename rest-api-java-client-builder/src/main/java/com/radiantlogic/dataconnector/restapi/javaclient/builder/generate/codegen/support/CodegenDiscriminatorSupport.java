package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support;

import lombok.NonNull;
import org.openapitools.codegen.CodegenModel;

/** Handles fixing discriminator property types. */
public class CodegenDiscriminatorSupport {
  public void fixDiscriminatorType(@NonNull final CodegenModel codegenModel) {
    if (codegenModel.discriminator == null) {
      return;
    }

    codegenModel.getVars().stream()
        .filter(prop -> prop.getBaseName().equals(codegenModel.discriminator.getPropertyBaseName()))
        .findFirst()
        .ifPresent(prop -> codegenModel.discriminator.setPropertyType(prop.getDatatypeWithEnum()));
  }
}

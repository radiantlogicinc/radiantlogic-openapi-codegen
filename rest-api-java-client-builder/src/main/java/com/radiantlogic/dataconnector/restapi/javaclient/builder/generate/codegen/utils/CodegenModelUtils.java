package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.openapitools.codegen.CodegenModel;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CodegenModelUtils {
  public static boolean hasDiscriminatorChildren(@NonNull final CodegenModel model) {
    return model.discriminator != null && model.discriminator.getMappedModels() != null;
  }
}

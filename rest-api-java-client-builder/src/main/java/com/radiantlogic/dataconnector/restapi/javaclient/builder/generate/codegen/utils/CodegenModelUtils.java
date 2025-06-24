package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.utils;

import com.radiantlogic.dataconnector.restapi.javaclient.builder.exceptions.ModelNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.ModelsMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CodegenModelUtils {
  public static boolean hasDiscriminatorChildren(@NonNull final CodegenModel model) {
    return model.discriminator != null && model.discriminator.getMappedModels() != null;
  }

  @NonNull
  public static CodegenModel extractModel(@NonNull final ModelsMap modelsMap) {
    return Optional.ofNullable(modelsMap.getModels())
        .filter(list -> !list.isEmpty())
        .map(List::getFirst)
        .map(ModelMap::getModel)
        .orElseThrow(
            () ->
                new ModelNotFoundException(
                    "ModelsMap had either no models or more than one model, cannot extract CodegenModel"));
  }
}

package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support;

import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.utils.CodegenModelUtils;
import java.util.Map;
import lombok.NonNull;
import org.openapitools.codegen.CodegenModel;

/** Handles fixing issues with discriminator configuration. */
public class CodegenDiscriminatorSupport {

  /**
   * The discriminator property type is usually just "String" but in many cases it is a more
   * specific (ie, enum) type. This fixes the discriminator type so it is accurate and the generated
   * code therefore will reflect that.
   */
  public void fixDiscriminatorType(@NonNull final CodegenModel codegenModel) {
    if (codegenModel.discriminator == null) {
      return;
    }

    codegenModel.getVars().stream()
        .filter(prop -> prop.getBaseName().equals(codegenModel.discriminator.getPropertyBaseName()))
        .findFirst()
        .ifPresent(prop -> codegenModel.discriminator.setPropertyType(prop.getDatatypeWithEnum()));
  }

  /**
   * Some of the jackson annotations in the mustache templates are configured with incorrect values
   * discriminated mappings. This adds a vendor extension property to all mapped models which is
   * then used by the customized template to correct this issue.
   */
  public void fixAllDiscriminatorMappings(@NonNull final Map<String, CodegenModel> allModels) {
    allModels.values().stream()
        .filter(CodegenModelUtils::hasDiscriminatorChildren)
        .forEach(model -> fixDiscriminatorMapping(model, allModels));
  }

  private void fixDiscriminatorMapping(
      @NonNull final CodegenModel codegenModel,
      @NonNull final Map<String, CodegenModel> allModels) {
    codegenModel
        .discriminator
        .getMappedModels()
        .forEach(
            mappedModel -> {
              final CodegenModel childModel = allModels.get(mappedModel.getModelName());
              // This is a special extension used in the template to ensure the correct
              // mapping value in the JsonTypeName annotation
              childModel.vendorExtensions.put(
                  "x-discriminator-mapping-value", mappedModel.getMappingName());
            });
  }
}

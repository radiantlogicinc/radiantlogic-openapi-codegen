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

  public UnionType getUnionType() {
    final boolean hasOneOf = oneOf != null && !oneOf.isEmpty();
    final boolean hasDiscriminator =
        discriminator != null && discriminator.getPropertyName() != null;
    final boolean hasMapping =
        discriminator != null
            && discriminator.getMappedModels() != null
            && !discriminator.getMappedModels().isEmpty();

    if (hasOneOf && hasDiscriminator && hasMapping) {
      return UnionType.DISCRIMINATED_UNION_WITH_MAPPING;
    }

    if (hasOneOf && hasDiscriminator) {
      return UnionType.DISCRIMINATED_UNION_NO_MAPPING;
    }

    if (hasOneOf) {
      return UnionType.UNION_NO_DISCRIMINATOR;
    }

    return UnionType.NO_UNION;
  }

  @Override
  public boolean getHasDiscriminatorWithNonEmptyMapping() {
    return getUnionType() == UnionType.DISCRIMINATED_UNION_WITH_MAPPING;
  }

  public boolean isInvalidUnionType() {
    return !getUnionType().isValid();
  }
}

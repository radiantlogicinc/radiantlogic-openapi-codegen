package com.radiantlogic.openapi.builder.javaclient.generate.codegen.support;

import java.util.List;
import java.util.stream.Stream;
import lombok.NonNull;
import org.openapitools.codegen.CodegenModel;

public record ExtractedEnumModels(
    @NonNull List<CodegenModel> enumsFromModelsWithParents,
    @NonNull List<CodegenModel> enumsFromDiscriminatorParentModels,
    @NonNull List<CodegenModel> enumsFromModelsWithNonDiscriminatorChildren) {
  @NonNull
  public List<CodegenModel> allEnums() {
    return Stream.of(
            enumsFromModelsWithParents,
            enumsFromDiscriminatorParentModels,
            enumsFromModelsWithNonDiscriminatorChildren)
        .flatMap(List::stream)
        .toList();
  }
}

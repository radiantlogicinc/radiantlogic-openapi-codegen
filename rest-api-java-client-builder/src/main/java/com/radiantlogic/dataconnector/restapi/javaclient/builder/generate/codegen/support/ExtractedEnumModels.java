package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support;

import java.util.List;
import lombok.NonNull;
import org.openapitools.codegen.CodegenModel;

public record ExtractedEnumModels(
    @NonNull List<CodegenModel> enumsFromModelsWithParents,
    @NonNull List<CodegenModel> enumsFromDiscriminatorParentModels,
    @NonNull List<CodegenModel> enumsFromModelsWithNonDiscriminatorChildren) {}

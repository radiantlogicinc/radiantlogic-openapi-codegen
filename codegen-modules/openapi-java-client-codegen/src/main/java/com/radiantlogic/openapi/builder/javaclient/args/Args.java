package com.radiantlogic.openapi.builder.javaclient.args;

import lombok.NonNull;
import lombok.With;

@With
public record Args(
    @NonNull ProgramArgStatus status, @NonNull String openapiPath, @NonNull String groupId) {}

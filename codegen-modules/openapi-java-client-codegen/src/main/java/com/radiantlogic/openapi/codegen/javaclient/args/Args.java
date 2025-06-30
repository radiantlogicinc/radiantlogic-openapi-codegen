package com.radiantlogic.openapi.codegen.javaclient.args;

import java.net.URL;
import lombok.NonNull;
import lombok.With;

@With
public record Args(
    @NonNull ProgramArgStatus status, @NonNull URL openapiUrl, @NonNull String groupId) {}

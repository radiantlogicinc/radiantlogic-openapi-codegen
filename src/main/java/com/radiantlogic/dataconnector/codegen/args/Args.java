package com.radiantlogic.dataconnector.codegen.args;

import lombok.NonNull;

public record Args(
    @NonNull ProgramArgStatus status, @NonNull String openapiPath, boolean doValidate) {}

package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen;

import io.swagger.v3.oas.models.OpenAPI;
import java.util.Set;
import lombok.NonNull;
import org.openapitools.codegen.CodegenConfig;

/** An extended API for codegen configuration. */
public interface ExtendedCodegenConfig extends CodegenConfig {
  /** Get any ignore patterns necessary to prevent excessive generation of unnecessary files. */
  @NonNull
  Set<String> getIgnorePatterns();

  /**
   * Initialize the codegen with all default configurations, providing the OpenAPI spec to do it.
   */
  void init(@NonNull final OpenAPI openAPI);
}

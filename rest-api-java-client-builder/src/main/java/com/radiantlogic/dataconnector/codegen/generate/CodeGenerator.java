package com.radiantlogic.dataconnector.codegen.generate;

import com.radiantlogic.dataconnector.codegen.args.Args;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.codegen.ClientOptInput;
import org.openapitools.codegen.DefaultGenerator;

/** The class that handles the actual code generation. */
@Slf4j
@RequiredArgsConstructor
public class CodeGenerator {
  @NonNull private final Args args;

  public void generate() {
    log.info("Generating code");
    final ParseOptions parseOptions = new ParseOptions();
    parseOptions.setResolve(true);
    parseOptions.setResolveFully(false);

    log.debug("Parsing OpenAPI specification");
    final OpenAPI openAPI =
        new OpenAPIParser().readLocation(args.openapiPath(), List.of(), parseOptions).getOpenAPI();

    preProcessOpenAPI(openAPI);

    log.debug("Performing code generation");
    final DataconnectorJavaClientCodegen codegen =
        new DataconnectorJavaClientCodegen(openAPI, args);
    new DefaultGenerator().opts(new ClientOptInput().config(codegen).openAPI(openAPI)).generate();
  }

  /** Perform any necessary pre-processing to resolve potential codegen issues. */
  private void preProcessOpenAPI(@NonNull final OpenAPI openAPI) {
    log.debug("Pre-processing OpenAPI");
    Optional.ofNullable(openAPI.getComponents())
        .map(Components::getSchemas)
        .orElseGet(Map::of)
        // Clear the title to prevent accidental identification of duplicate schemas
        .forEach((key, schema) -> schema.setTitle(null));
  }
}

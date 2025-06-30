package com.radiantlogic.openapi.codegen.javaclient.generate;

import com.radiantlogic.openapi.codegen.javaclient.args.Args;
import com.radiantlogic.openapi.codegen.javaclient.exceptions.OpenapiParseException;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Parse the OpenAPI specification. */
@Slf4j
@RequiredArgsConstructor
public class OpenapiParser {
  @NonNull private final Args args;
  @NonNull private final OpenAPIParser parser;

  public OpenapiParser(@NonNull final Args args) {
    this.args = args;
    this.parser = new OpenAPIParser();
  }

  @NonNull
  public OpenAPI parse() {
    log.info("Parsing OpenAPI specification");
    final ParseOptions parseOptions = new ParseOptions();
    parseOptions.setResolve(true);
    parseOptions.setResolveFully(false);

    try {
      final Path tempFile = Files.createTempFile("openapi", ".yaml");
      try (InputStream stream = args.openapiUrl().openStream()) {
        Files.copy(stream, tempFile);
      }

      final OpenAPI openAPI =
          parser.readLocation(tempFile.toString(), List.of(), parseOptions).getOpenAPI();
      if (openAPI == null) {
        throw new OpenapiParseException("OpenAPI parse result is null");
      }
      return openAPI;
    } catch (final Exception ex) {
      throw new OpenapiParseException(
          "Failed to parse OpenAPI: %s".formatted(args.openapiUrl()), ex);
    }
  }
}

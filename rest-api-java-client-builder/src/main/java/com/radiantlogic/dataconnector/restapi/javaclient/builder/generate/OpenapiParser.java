package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate;

import com.radiantlogic.dataconnector.restapi.javaclient.builder.args.Args;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.exceptions.OpenapiParseException;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Parse the OpenAPI specification. */
@Slf4j
@RequiredArgsConstructor
public class OpenapiParser {
  @NonNull private final Args args;

  @NonNull
  public OpenAPI parse() {
    log.info("Parsing OpenAPI specification");
    final ParseOptions parseOptions = new ParseOptions();
    parseOptions.setResolve(true);
    parseOptions.setResolveFully(false);

    final OpenAPIParser parser = new OpenAPIParser();

    try {
      final OpenAPI openAPI =
          parser.readLocation(args.openapiPath(), List.of(), parseOptions).getOpenAPI();
      if (openAPI == null) {
        throw new OpenapiParseException("OpenAPI parse result is null");
      }
      return openAPI;
    } catch (final Exception ex) {
      throw new OpenapiParseException(
          "Failed to parse OpenAPI: %s".formatted(args.openapiPath()), ex);
    }
  }
}

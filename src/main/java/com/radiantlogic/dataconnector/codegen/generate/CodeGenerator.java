package com.radiantlogic.dataconnector.codegen.generate;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.openapitools.codegen.ClientOptInput;
import org.openapitools.codegen.DefaultGenerator;

@RequiredArgsConstructor
public class CodeGenerator {
  @NonNull private final ParsedConfig config;

  public void generate() {
    final SwaggerParseResult parseResult =
        new OpenAPIParser().readLocation(config.openapiPath(), List.of(), new ParseOptions());
    final DataconnectorJavaClientCodegen codegen =
        new DataconnectorJavaClientCodegen(parseResult.getOpenAPI());
    new DefaultGenerator()
        .opts(new ClientOptInput().config(codegen).openAPI(parseResult.getOpenAPI()))
        .generate();
  }
}

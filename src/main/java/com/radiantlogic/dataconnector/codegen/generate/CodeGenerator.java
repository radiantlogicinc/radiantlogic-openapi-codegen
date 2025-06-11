package com.radiantlogic.dataconnector.codegen.generate;

import com.radiantlogic.dataconnector.codegen.args.Args;
import io.swagger.v3.parser.core.models.ParseOptions;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CodeGenerator {
  @NonNull private final Args args;

  public void generate() {
    final ParseOptions parseOptions = new ParseOptions();
    parseOptions.setResolve(true);
    parseOptions.setResolveFully(true);

    //    final OpenAPI openAPI =
    //        new OpenAPIParser()
    //            .readLocation(config.openapiPath(), List.of(), parseOptions)
    //            .getOpenAPI();
    //    final DataconnectorJavaClientCodegen codegen = new
    // DataconnectorJavaClientCodegen(openAPI);
    //    new DefaultGenerator().opts(new
    // ClientOptInput().config(codegen).openAPI(openAPI)).generate();
  }
}

package com.radiantlogic.openapi.codegen.javaclient;

import com.radiantlogic.openapi.codegen.javaclient.args.Args;
import com.radiantlogic.openapi.codegen.javaclient.args.ArgsParser;
import com.radiantlogic.openapi.codegen.javaclient.args.ProgramArgStatus;
import com.radiantlogic.openapi.codegen.javaclient.generate.CodeGeneratorExecutor;
import com.radiantlogic.openapi.codegen.javaclient.generate.OpenapiParser;
import com.radiantlogic.openapi.codegen.javaclient.generate.codegen.DataconnectorJavaClientCodegen;
import com.radiantlogic.openapi.codegen.javaclient.openapi.OpenapiPathValidator;
import com.radiantlogic.openapi.codegen.javaclient.properties.Props;
import com.radiantlogic.openapi.codegen.javaclient.properties.PropsReader;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Runner {

  /**
   * If run via IntelliJ, this will not work unless you do the following:
   *
   * <p>1. Open the run configuration.
   *
   * <p>2. Select Modify Options -> Before Launch Task
   *
   * <p>3. Add a new task, "Add Maven Goal"
   *
   * <p>4. The "command line options" should be "generate-resources"
   *
   * <p>This is because IntelliJ by default doesn't run the maven lifecycle, it runs its own system.
   * This program generates a properties file at compile time with the maven-resources-plugin, which
   * is necessary.
   *
   * <p>In addition, make sure the IntelliJ working directory is set to
   * rest-api-java-client-builder.
   */
  public static void main(final String[] args) {
    try {
      new Runner().run(args);
    } catch (final Exception ex) {
      log.error("Application has failed to execute", ex);
      System.exit(1);
    }
  }

  public void run(final String[] args) throws Exception {
    final PropsReader propsReader = new PropsReader();
    final Props props = propsReader.readProps();

    log.info("Starting code generation");
    final ArgsParser argsParser = new ArgsParser(props);
    final String[] argsAfterDevHandling = handleDevArgs(args);
    final Args parsedArgs = argsParser.parse(argsAfterDevHandling);
    if (parsedArgs.status() == ProgramArgStatus.EXIT) {
      System.exit(0);
      return;
    }

    // TODO this whole thing is more than a bit of a mess here...
    final OpenapiPathValidator openapiPathValidator = new OpenapiPathValidator();
    final String parsedPath = openapiPathValidator.parseAndValidate(parsedArgs.openapiPath());
    // TODO if a URL, need to pull file down
    final Args validatedParsedArgs = parsedArgs.withOpenapiPath(parsedPath);
    log.info("Path to OpenAPI specification: {}", validatedParsedArgs.openapiPath());

    final OpenapiParser openapiParser = new OpenapiParser(validatedParsedArgs);
    final DataconnectorJavaClientCodegen codegen =
        new DataconnectorJavaClientCodegen(validatedParsedArgs);
    final CodeGeneratorExecutor codeGenerator = new CodeGeneratorExecutor(codegen);

    log.info("Parsing and generating code");
    final OpenAPI openAPI = openapiParser.parse();
    codegen.init(openAPI);
    codeGenerator.generate(openAPI);
    log.info("Finished code generation");
  }

  /**
   * Handling args in development mode slightly differently than production allows for this program
   * to work more gracefully with the exec-maven-plugin from the CLI.
   *
   * <p>The expectation is that, if the first arg is 'dev', then the section arg will be a
   * space-separated list of all other arguments, which is what the program actually expects.
   */
  private static String[] handleDevArgs(@NonNull final String[] args) {
    if (args.length == 2 && args[0].equals("dev")) {
      log.debug("Dev args detected, applying special handling");
      return args[1].split(" ");
    }
    return args;
  }
}

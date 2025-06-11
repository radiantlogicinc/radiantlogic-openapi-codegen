package com.radiantlogic.dataconnector.codegen.args;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class ArgsParser {
  private static final Option PATH_OPTION =
      Option.builder()
          .argName("p")
          .longOpt("path")
          .desc("The path to the OpenAPI specification. Either a file path or URL.")
          .type(String.class)
          .hasArg()
          .valueSeparator('=')
          .required()
          .build();
  private static final Option VALIDATE_OPTION =
      Option.builder()
          .argName("v")
          .longOpt("validate")
          .desc(
              "Whether or not to run validation on the OpenAPI specification before generating code. Strongly recommended. Defaults to true.")
          .type(Boolean.class)
          .required(false)
          .build();
  private static final Option HELP_OPTION =
      Option.builder()
          .argName("h")
          .longOpt("help")
          .desc("Print the help information for this CLI")
          .type(Boolean.class)
          .required(false)
          .build();

  public Args parse(final String[] args) {
    if (args.length <= 1) {
      throw new IllegalArgumentException(
          "Missing required arguments. Please run with -h for more information.");
    }

    final Options options = new Options();
    options.addOption(PATH_OPTION);
    options.addOption(VALIDATE_OPTION);
    options.addOption(HELP_OPTION);
  }
}

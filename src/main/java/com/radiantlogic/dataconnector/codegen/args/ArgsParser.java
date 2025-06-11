package com.radiantlogic.dataconnector.codegen.args;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

@Slf4j
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

    log.debug("Parsing cli arguments {}", Arrays.toString(args));

    final Options options = new Options();
    options.addOption(PATH_OPTION);
    options.addOption(VALIDATE_OPTION);
    options.addOption(HELP_OPTION);

    try {
      final CommandLineParser parser = new DefaultParser();
      final CommandLine commandLine = parser.parse(options, args);
      if (commandLine.hasOption(HELP_OPTION.getArgName())) {
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("rest-api-java-client-builder", options);
      }
    } catch (final ParseException ex) {
      throw new IllegalStateException(
          "Failed to parse command line arguments, cannot proceed.", ex);
    }
  }
}

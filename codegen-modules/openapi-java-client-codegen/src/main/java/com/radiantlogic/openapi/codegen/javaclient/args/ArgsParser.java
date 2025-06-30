package com.radiantlogic.openapi.codegen.javaclient.args;

import com.radiantlogic.openapi.codegen.javaclient.properties.Props;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

/** Parse the arguments supplied to this program using the commons-cli library. */
@Slf4j
@RequiredArgsConstructor
public class ArgsParser {
  public static final String FILE_PROTOCOL = "file:";
  public static final String DEFAULT_GROUP_ID = "com.radiantlogic.openapi.generated";

  private static final Option PATH_OPTION =
      Option.builder("p")
          .argName("OpenAPI Path")
          .longOpt("path")
          .desc("The path to the OpenAPI specification. Either a file path or URL.")
          .hasArg()
          .valueSeparator('=')
          .build();
  private static final Option GROUP_ID_OPTION =
      Option.builder("g")
          .argName("Group ID")
          .longOpt("groupId")
          .desc(
              "The groupId to use for the generated artifact. If not provided, a default groupId will be set")
          .hasArg()
          .valueSeparator('=')
          .build();
  private static final Option HELP_OPTION =
      Option.builder("h")
          .argName("Help")
          .longOpt("help")
          .desc("Print the help information for this CLI")
          .build();
  private static final Options OPTIONS = new Options();

  static {
    OPTIONS.addOption(PATH_OPTION);
    OPTIONS.addOption(HELP_OPTION);
    OPTIONS.addOption(GROUP_ID_OPTION);
  }

  @NonNull private final Props props;

  @NonNull
  public Args parse(@NonNull final String[] args) {
    if (args.length < 1) {
      throw new IllegalArgumentException(
          "Missing required arguments. Please run with -h for more information.");
    }

    log.debug("Parsing cli arguments {}", Arrays.toString(args));

    try {
      final CommandLineParser parser = new DefaultParser();
      final CommandLine commandLine = parser.parse(OPTIONS, args);

      if (commandLine.hasOption(HELP_OPTION.getOpt())) {
        return handleHelp();
      }

      final String openapiPath = commandLine.getOptionValue(PATH_OPTION.getOpt(), "");
      if (StringUtils.isBlank(openapiPath)) {
        throw new IllegalArgumentException(
            "Missing required OpenAPI path argument. Please use the -h option to see usage instructions.");
      }

      final String groupId = commandLine.getOptionValue(GROUP_ID_OPTION.getOpt(), DEFAULT_GROUP_ID);
      final URL openapiUrl = parseOpenapiPath(openapiPath);
      return new Args(ProgramArgStatus.PROCEED, openapiUrl, groupId);
    } catch (final ParseException ex) {
      throw new IllegalArgumentException(
          "Failed to parse command line arguments, cannot proceed.", ex);
    }
  }

  @NonNull
  private URL parseOpenapiPath(@NonNull final String openapiPath) {
    try {
      return new URI(openapiPath).toURL();
    } catch (final MalformedURLException | URISyntaxException ex) {
      try {
        final Path absoluteFilePath = Paths.get(openapiPath);
        if (Files.exists(absoluteFilePath)) {
          return new URI("%s%s".formatted(FILE_PROTOCOL, absoluteFilePath)).toURL();
        }

        final Path relativePath = Paths.get(System.getProperty("user.dir"), openapiPath);
        if (Files.exists(relativePath)) {
          return new URI("%s%s".formatted(FILE_PROTOCOL, relativePath)).toURL();
        }
        throw new FileNotFoundException(
            "Cannot find path on filesystem: %s".formatted(openapiPath));
      } catch (final MalformedURLException | URISyntaxException | FileNotFoundException ex2) {
        ex2.addSuppressed(ex);
        throw new IllegalArgumentException(
            "Cannot parse OpenAPI path as either URL or file: %s".formatted(openapiPath), ex2);
      }
    }
  }

  @NonNull
  private Args handleHelp() {
    final HelpFormatter helpFormatter = new HelpFormatter();

    final String header =
        """

            NOTE: arguments with values are separated by an '=' sign, ie -p=http://localhost:8080/openapi.json

            """
            .stripIndent();

    helpFormatter.printHelp(
        "%s %s".formatted(props.artifactId(), props.version()), header, OPTIONS, "Footer", true);
    try {
      return new Args(ProgramArgStatus.EXIT, new URI("http://localhost").toURL(), "");
    } catch (final Exception ex) {
      throw new IllegalStateException("Should not be possible for this exception to be thrown");
    }
  }
}

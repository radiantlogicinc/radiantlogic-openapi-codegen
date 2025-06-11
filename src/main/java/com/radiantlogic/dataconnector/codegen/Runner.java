package com.radiantlogic.dataconnector.codegen;

import com.radiantlogic.dataconnector.codegen.args.Args;
import com.radiantlogic.dataconnector.codegen.args.ArgsParser;
import com.radiantlogic.dataconnector.codegen.args.ProgramArgStatus;
import com.radiantlogic.dataconnector.codegen.properties.Props;
import com.radiantlogic.dataconnector.codegen.properties.PropsReader;
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
   */
  public static void main(final String[] args) {
    try {
      if (args.length == 0) {
        throw new IllegalArgumentException("Cannot execute with specifying the command");
      }

      final PropsReader propsReader = new PropsReader();
      final Props props = propsReader.readProps();

      log.info("Starting {}", props.artifactId());
      final ArgsParser argsParser = new ArgsParser(props);
      final Args parsedArgs = argsParser.parse(args);
      if (parsedArgs.status() == ProgramArgStatus.EXIT) {
        System.exit(0);
        return;
      }
      // TODO run program
    } catch (final Exception ex) {
      log.error("Application has failed to execute", ex);
      System.exit(1);
    }
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

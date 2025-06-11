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
  public static void main(final String[] args) {
    try {
      if (args.length == 0) {
        throw new IllegalArgumentException("Cannot execute with specifying the command");
      }

      final PropsReader propsReader = new PropsReader();
      final Props props = propsReader.readProps();

      log.info("Starting {}", props.artifactId());
      final ArgsParser argsParser = new ArgsParser();
      final Args parsedArgs = argsParser.parse(args);
      if (parsedArgs.status() == ProgramArgStatus.EXIT) {
        System.exit(0);
      }
      // TODO run program
    } catch (final Exception ex) {
      log.error("Application has failed to execute", ex);
      System.exit(1);
    }
  }

  private static String[] handleDevArgs(@NonNull final String[] args) {}
}

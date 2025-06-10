package com.radiantlogic.dataconnector.codegen;

import com.radiantlogic.dataconnector.codegen.generate.CodeGenerator;
import com.radiantlogic.dataconnector.codegen.generate.ConfigParser;
import com.radiantlogic.dataconnector.codegen.generate.ParsedConfig;

public class Runner {
  private static final String COMMAND_GENERATE = "generate";

  public static void main(final String[] args) {
    if (args.length == 0) {
      throw new IllegalArgumentException("Cannot execute with specifying the command");
    }

    if (COMMAND_GENERATE.equals(args[0])) {
      final ParsedConfig config = new ConfigParser().parseArgs(args);
      new CodeGenerator(config).generate();
      return;
    }

    throw new IllegalArgumentException("Unknown command: %s".formatted(args[0]));
  }
}

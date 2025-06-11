package com.radiantlogic.dataconnector.codegen;

import com.radiantlogic.dataconnector.codegen.args.Args;
import com.radiantlogic.dataconnector.codegen.args.ArgsParser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Runner {
  public static void main(final String[] args) {
    if (args.length == 0) {
      throw new IllegalArgumentException("Cannot execute with specifying the command");
    }

    log.info("Starting program");
    final ArgsParser argsParser = new ArgsParser();
    final Args parsedArgs = argsParser.parse(args);
    // TODO run program
  }
}

package com.radiantlogic.dataconnector.codegen.args;

import static org.assertj.core.api.Assertions.assertThat;

import com.radiantlogic.dataconnector.codegen.properties.Props;
import org.junit.jupiter.api.Test;

public class ArgsParserTest {
  final Props props = new Props("my-artifact", "1.0.0");
  private final ArgsParser argsParser = new ArgsParser(props);

  @Test
  void itShowsHelpMenu() {
    final String[] args = {"-h"};
    final Args parsedArgs = argsParser.parse(args);
    final Args expectedArgs = new Args(ProgramArgStatus.EXIT, "", "", false);
    assertThat(parsedArgs).isEqualTo(expectedArgs);
  }

  @Test
  void itParsesWithDefaults() {
    throw new RuntimeException();
  }

  @Test
  void itParsesWithAllArgsProvided() {
    throw new RuntimeException();
  }
}

package com.radiantlogic.dataconnector.codegen.generate;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigParser {
  @NonNull
  public ParsedConfig parseArgs(@NonNull final String[] args) {
    if (args.length <= 1) {
      throw new IllegalArgumentException("No codegen arguments specified");
    }

    log.debug("Parsing codegen arguments: {}", String.join(" ", args));

    final String openapiPath = args[1];
  }
}

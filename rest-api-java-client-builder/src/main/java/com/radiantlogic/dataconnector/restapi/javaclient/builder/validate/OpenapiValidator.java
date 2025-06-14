package com.radiantlogic.dataconnector.restapi.javaclient.builder.validate;

import com.radiantlogic.dataconnector.restapi.javaclient.builder.args.Args;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenapiValidator {
  private static final Path VALIDATOR_PATH =
      Paths.get(System.getProperty("user.dir"), "redocly-validator");
  private static final Duration VALIDATION_WAIT_TIME = Duration.ofMinutes(2);

  public void validate(@NonNull final Args args) {
    if (!args.doValidate()) {
      log.info("Skipping validation of openapi specification");
    }

    log.info("Performing validation of openapi specification");
    try {
      final Process process =
          new ProcessBuilder("npm", "run", "lint", "--", args.openapiPath())
              .directory(VALIDATOR_PATH.toFile())
              .inheritIO()
              .start();

      final boolean waitSuccess = process.waitFor(VALIDATION_WAIT_TIME);
      if (!waitSuccess) {
        throw new IOException("Wait for validation process failed");
      }

      final int code = process.exitValue();
      if (code != 0) {
        throw new IOException("Invalid exit status: %d".formatted(code));
      }
    } catch (final IOException | InterruptedException ex) {
      throw new IllegalStateException(
          "Openapi specification validation failed, cannot proceed", ex);
    }
  }
}

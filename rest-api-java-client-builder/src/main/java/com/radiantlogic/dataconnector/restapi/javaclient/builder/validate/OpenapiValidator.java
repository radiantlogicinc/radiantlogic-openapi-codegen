package com.radiantlogic.dataconnector.restapi.javaclient.builder.validate;

import com.radiantlogic.dataconnector.restapi.javaclient.builder.args.Args;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.exceptions.OpenapiValidationException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OpenapiValidator {
  private static final Path VALIDATOR_PATH =
      Paths.get(System.getProperty("user.dir"), "redocly-validator");
  private static final Duration VALIDATION_WAIT_TIME = Duration.ofMinutes(2);

  @NonNull private final Args args;

  public void validate() {
    if (!args.doValidate()) {
      log.info("Skipping validation of openapi specification");
    }

    final String pathWithoutFilePrefix = args.openapiPath().replaceAll("^file:", "");
    log.info("Performing validation of openapi specification: {}", pathWithoutFilePrefix);
    try {
      final Process process =
          new ProcessBuilder("npm", "run", "lint", "--", pathWithoutFilePrefix)
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
      throw new OpenapiValidationException(
          "Openapi specification validation failed, cannot proceed", ex);
    }
  }
}

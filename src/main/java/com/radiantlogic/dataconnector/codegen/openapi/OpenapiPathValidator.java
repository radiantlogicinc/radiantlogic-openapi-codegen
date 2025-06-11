package com.radiantlogic.dataconnector.codegen.openapi;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Parse and validate the OpenAPI specification path to ensure it is valid before running any
 * validation or code generation on it.
 */
@Slf4j
public class OpenapiPathValidator {
  public String parseAndValidate(@NonNull final String openapiPath) {
    log.info("Parsing and validating OpenAPI path: {}", openapiPath);
    try {
      final URI uri = new URI(openapiPath);
      if (openapiPath.startsWith("file:")) {
        ensureFileUriExists(uri);
      }
      return uri.toString();
    } catch (final URISyntaxException | IllegalArgumentException ex) {
      log.debug("Failed to parse OpenAPI path as URI. Trying as file path.");
      try {
        final URI fileUri = new URI("file://%s".formatted(openapiPath));
        ensureFileUriExists(fileUri);
        return fileUri.toString();
      } catch (final URISyntaxException | IllegalArgumentException ex2) {
        ex.addSuppressed(ex2);
      }

      throw createInvalidPathException(ex);
    }
  }

  private void ensureFileUriExists(@NonNull final URI fileUri) {
    if (!Files.exists(Paths.get(fileUri))) {
      throw new IllegalArgumentException("File does not exist: %s".formatted(fileUri));
    }
  }

  private IllegalArgumentException createInvalidPathException(final Throwable cause) {
    return new IllegalArgumentException("Invalid OpenAPI path. Must be valid URL or file.", cause);
  }
}

package com.radiantlogic.dataconnector.codegen.openapi;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
  // TODO add relative path support
  public String parseAndValidate(@NonNull final String openapiPath) {
    log.info("Parsing and validating OpenAPI path: {}", openapiPath);
    try {
      final URI uri = new URI(openapiPath);
      final URL url = uri.toURL();
      if (openapiPath.startsWith("file:")) {
        ensureFileUriExists(uri);
      }
      return url.toString();
    } catch (final URISyntaxException | MalformedURLException | IllegalArgumentException ex) {
      log.debug("Failed to parse OpenAPI path as URI. Trying as file path.");
      try {
        final URI fileUri = new URI("file://%s".formatted(openapiPath));
        ensureFileUriExists(fileUri);
        return fileUri.toURL().toString();
      } catch (final URISyntaxException | MalformedURLException | IllegalArgumentException ex2) {
        ex.addSuppressed(ex2);
      }

      throw createInvalidPathException(ex, openapiPath);
    }
  }

  private void ensureFileUriExists(@NonNull final URI fileUri) {
    if (!Files.exists(Paths.get(fileUri))) {
      throw new IllegalArgumentException("File does not exist: %s".formatted(fileUri));
    }
  }

  private IllegalArgumentException createInvalidPathException(
      final Throwable cause, final String path) {
    return new IllegalArgumentException(
        "Invalid OpenAPI path. Must be valid URL or file. Path: %s".formatted(path), cause);
  }
}

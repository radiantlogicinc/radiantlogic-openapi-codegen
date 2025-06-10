package com.radiantlogic.dataconnector.codegen.generate;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
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
    try {
      final URL url = new URI(openapiPath).toURL();
      return new ParsedConfig(url.toString());
    } catch (final URISyntaxException | MalformedURLException | IllegalArgumentException ex) {
      log.debug("Failed to parse OpenAPI path as URL. Trying as file path.");
      try {
        final URI fileUri = new URI("file://%s".formatted(openapiPath));
        if (!Files.exists(Paths.get(fileUri))) {
          throw new IllegalArgumentException("File does not exist: %s".formatted(openapiPath));
        }
        return new ParsedConfig(fileUri.toURL().toString());
      } catch (final URISyntaxException | MalformedURLException | IllegalArgumentException ex2) {
        ex.addSuppressed(ex2);
      }

      throw createInvalidPathException(ex);
    }
  }

  private IllegalArgumentException createInvalidPathException(final Throwable cause) {
    return new IllegalArgumentException("Invalid OpenAPI path. Must be valid URL or file.", cause);
  }
}

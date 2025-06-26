package com.radiantlogic.openapi.usage.javaclient;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.springframework.lang.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceReader {
  @SneakyThrows
  public static String readString(@NonNull final String resourceName) {
    try (InputStream stream = openStream(resourceName)) {
      return IOUtils.toString(stream, StandardCharsets.UTF_8);
    }
  }

  private static URL getResourceUrl(@NonNull final String resourceName) {
    final URL url = ResourceReader.class.getClassLoader().getResource(resourceName);
    if (url == null) {
      throw new IllegalArgumentException(String.format("Resource not found: %s", resourceName));
    }
    return url;
  }

  @SneakyThrows
  private static InputStream openStream(@NonNull final String resourceName) {
    return getResourceUrl(resourceName).openStream();
  }

  @SneakyThrows
  public static byte[] readBytes(@NonNull final String resourceName) {
    try (InputStream stream = openStream(resourceName)) {
      return IOUtils.toByteArray(stream);
    }
  }

  @SneakyThrows
  public static Path getFilePath(@NonNull final String resourceName) {
    return Paths.get(getResourceUrl(resourceName).toURI());
  }
}

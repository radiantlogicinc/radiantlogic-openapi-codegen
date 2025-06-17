package com.radiantlogic.dataconnector.client.usage;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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

  private static InputStream openStream(@NonNull final String resourceName) {
    final InputStream stream =
        DiscriminatedUnionSerdeTest.class.getClassLoader().getResourceAsStream(resourceName);
    if (stream == null) {
      throw new IllegalArgumentException(String.format("Resource not found: %s", resourceName));
    }
    return stream;
  }

  @SneakyThrows
  public static byte[] readBytes(@NonNull final String resourceName) {
    try (InputStream stream = openStream(resourceName)) {
      return IOUtils.toByteArray(stream);
    }
  }
}

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
  public static String read(@NonNull final String resourceName) {
    final InputStream stream =
        DiscriminatedUnionSerdeTest.class.getClassLoader().getResourceAsStream(resourceName);
    if (stream == null) {
      throw new IllegalArgumentException("Resource not found: " + resourceName);
    }

    try {
      return IOUtils.toString(stream, StandardCharsets.UTF_8);
    } finally {
      stream.close();
    }
  }
}

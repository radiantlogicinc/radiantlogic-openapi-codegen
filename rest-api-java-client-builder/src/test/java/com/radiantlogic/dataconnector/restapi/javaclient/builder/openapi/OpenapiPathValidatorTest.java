package com.radiantlogic.dataconnector.restapi.javaclient.builder.openapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class OpenapiPathValidatorTest {
  private final OpenapiPathValidator openapiPathValidator = new OpenapiPathValidator();
  @TempDir private Path tempDir;

  @Test
  void itRejectsInvalidOpenapiPath() {
    assertThatThrownBy(() -> openapiPathValidator.parseAndValidate("abcdefg"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid OpenAPI path. Must be valid URL or file.");
  }

  @Test
  void itAcceptsUrlOpenapiPath() {
    final String url = "https://localhost:8080/openapi.json";
    final String parsedUrl = openapiPathValidator.parseAndValidate(url);
    assertThat(parsedUrl).isEqualTo(url);
  }

  @Test
  @SneakyThrows
  void itAcceptsUrlFileOpenapiPath() {
    final Path tempFile = tempDir.resolve("openapi.json");
    Files.createFile(tempFile);
    final String file = "file://%s".formatted(tempFile.toString());
    final String parsedFile = openapiPathValidator.parseAndValidate(file);
    final String expectedParsedFile = "file:%s".formatted(tempFile.toString());
    assertThat(parsedFile).isEqualTo(expectedParsedFile);
  }

  @Test
  @SneakyThrows
  void itAcceptsFileOpenapiPath() {
    final Path tempFile = tempDir.resolve("openapi.json");
    Files.createFile(tempFile);
    final String parsedFile = openapiPathValidator.parseAndValidate(tempFile.toString());
    final String expectedParsedFile = "file:%s".formatted(tempFile.toString());
    assertThat(parsedFile).isEqualTo(expectedParsedFile);
  }
}

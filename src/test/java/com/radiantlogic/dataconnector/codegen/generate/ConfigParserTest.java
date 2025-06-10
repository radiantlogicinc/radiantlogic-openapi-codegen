package com.radiantlogic.dataconnector.codegen.generate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ConfigParserTest {
  private final ConfigParser configParser = new ConfigParser();
  @TempDir private Path tempDir;

  @Test
  void itRejectsNoArgs() {
    assertThatThrownBy(() -> configParser.parseArgs(new String[] {}))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("No codegen arguments specified");
  }

  @Test
  void itRejectsInvalidOpenapiPath() {
    assertThatThrownBy(() -> configParser.parseArgs(new String[] {"generate", "abcdefg"}))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid OpenAPI path. Must be valid URL or file.");
  }

  @Test
  void itAcceptsUrlOpenapiPath() {
    final String url = "https://localhost:8080/openapi.json";
    final ParsedConfig config = configParser.parseArgs(new String[] {"generate", url});
    final ParsedConfig expected = new ParsedConfig(url);
    assertThat(config).isEqualTo(expected);
  }

  @Test
  void itAcceptsUrlFileOpenapiPath() {
    final String file = "file:///tmp/openapi.json";
    final ParsedConfig config = configParser.parseArgs(new String[] {"generate", file});
    final ParsedConfig expected = new ParsedConfig("file:/tmp/openapi.json");
    assertThat(config).isEqualTo(expected);
  }

  @Test
  @SneakyThrows
  void itAcceptsFileOpenapiPath() {
    final Path tempFile = tempDir.resolve("openapi.json");
    Files.createFile(tempFile);
    final ParsedConfig config =
        configParser.parseArgs(new String[] {"generate", tempFile.toString()});
    final ParsedConfig expected = new ParsedConfig("file:%s".formatted(tempFile.toString()));
    assertThat(config).isEqualTo(expected);
  }
}

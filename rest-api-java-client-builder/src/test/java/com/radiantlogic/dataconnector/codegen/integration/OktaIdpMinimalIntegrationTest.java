package com.radiantlogic.dataconnector.codegen.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.radiantlogic.dataconnector.codegen.Runner;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class OktaIdpMinimalIntegrationTest {
  private static final String OPENAPI_YAML = "openapi/okta-idp-minimal-2025.01.1.yaml";
  private static final String OUTPUT_DIR = "output/MyAccount-Management/2025.01.1";
  private static final Duration WAIT_FOR_BUILD = Duration.ofMinutes(2);

  @Test
  @SneakyThrows
  void itGeneratesAndBuilds() {
    final URL url = getClass().getClassLoader().getResource(OPENAPI_YAML);
    final Path yamlPath = Paths.get(url.toURI());
    final Runner runner = new Runner();
    final String[] args = new String[] {"-p=%s".formatted(yamlPath.toString())};
    runner.run(args);

    final Path outputPath = Paths.get(System.getProperty("user.dir"), OUTPUT_DIR);
    final Process process =
        new ProcessBuilder("mvn", "clean", "install", "-DskipTests")
            .directory(outputPath.toFile())
            .inheritIO()
            .start();

    final boolean waitSuccess = process.waitFor(WAIT_FOR_BUILD);
    assertThat(waitSuccess).withFailMessage("Wait for build of generated code timed out.").isTrue();

    final int exitValue = process.exitValue();
    assertThat(exitValue).isEqualTo(0);
  }
}

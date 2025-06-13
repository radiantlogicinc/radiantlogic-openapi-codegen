package com.radiantlogic.dataconnector.codegen.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.radiantlogic.dataconnector.codegen.Runner;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class CodegenIntegrationTest {
  private static final Path OUTPUT_DIR = Paths.get(System.getProperty("user.dir"), "output");
  private static final Duration WAIT_FOR_BUILD = Duration.ofMinutes(2);

  static Stream<Arguments> codegenArgs() {
    return Stream.of(
        Arguments.arguments("okta-idp-minimal-2025.01.1.yaml", "MyAccount-Management/2025.01.1"));
  }

  @BeforeEach
  @SneakyThrows
  void beforeEach() {
    FileUtils.deleteDirectory(OUTPUT_DIR.toFile());
  }

  @ParameterizedTest(name = "Generates and builds code for {0}")
  @MethodSource("codegenArgs")
  @SneakyThrows
  void itGeneratesAndBuilds(
      @NonNull final String yamlFilename, @NonNull final String relativeOutputPath) {
    final URL url = getClass().getClassLoader().getResource("openapi/%s".formatted(yamlFilename));
    final Path yamlPath = Paths.get(url.toURI());
    final Runner runner = new Runner();
    final String[] args = new String[] {"-p=%s".formatted(yamlPath.toString())};
    runner.run(args);

    final Path outputPath = OUTPUT_DIR.resolve(relativeOutputPath);
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

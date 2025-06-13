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

/** Integration tests that validate this codegen against various openapi specifications. */
public class CodegenIntegrationTest {
  private static final Path OUTPUT_DIR = Paths.get(System.getProperty("user.dir"), "output");
  private static final Duration WAIT_FOR_BUILD = Duration.ofMinutes(2);

  static Stream<Arguments> codegenArgs() {
    return Stream.of(
        Arguments.arguments("okta-idp-minimal-2025.01.1.yaml", "MyAccount-Management/2025.01.1"),
        Arguments.arguments("okta-management-minimal-2025.01.1", "Okta-Admin-Management/2025.01.1"),
        Arguments.arguments(
            "okta-oauth-minimal-2025.01.1.yaml", "Okta-OpenID-Connect--OAuth-2.0/2025.01.1"),
        Arguments.arguments(
            "radiantone-openapi-8.1.4-beta.2-SNAPSHOT.yaml", "RadiantOne-V8-API/2025.01.1"));
  }

  @BeforeEach
  @SneakyThrows
  void beforeEach() {
    FileUtils.deleteDirectory(OUTPUT_DIR.toFile());
  }

  /**
   * If run via IntelliJ, this will not work unless you do the following:
   *
   * <p>1. Open the run configuration for the test.
   *
   * <p>2. Select Modify Options -> Before Launch Task
   *
   * <p>3. Add a new task, "Add Maven Goal"
   *
   * <p>4. The "command line options" should be "generate-resources"
   *
   * <p>This is because IntelliJ by default doesn't run the maven lifecycle, it runs its own system.
   * This test depends on a properties file generated at compile time with the
   * maven-resources-plugin, which is necessary.
   */
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

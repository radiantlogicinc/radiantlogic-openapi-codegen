package com.radiantlogic.dataconnector.restapi.javaclient.builder.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.radiantlogic.dataconnector.restapi.javaclient.builder.Runner;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.exceptions.OpenapiValidationException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

/**
 * Integration tests that validate this codegen against various openapi specifications. If run via
 * IntelliJ, this will not work unless you do the following:
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
 * This test depends on a properties file generated at compile time with the maven-resources-plugin,
 * which is necessary.
 */
public class CodegenIT {
  private static final Path OUTPUT_DIR = Paths.get(System.getProperty("user.dir"), "output");
  private static final Duration WAIT_FOR_BUILD = Duration.ofMinutes(2);

  @Test
  void oktaIdpMinimalSuccess() {
    generateAndBuild("okta-idp-minimal-2025.01.1.yaml", "MyAccount-Management/2025.01.1");
  }

  @Test
  void oktaManagementMinimalFailure() {
    assertThatThrownBy(
            () ->
                generateAndBuild(
                    "okta-management-minimal-2025.01.1.yaml", "Okta-Admin-Management/2025.01.1"))
        .isInstanceOf(OpenapiValidationException.class);
  }

  @Test
  void oktaOauthMinimalSuccess() {
    generateAndBuild(
        "okta-oauth-minimal-2025.01.1.yaml", "Okta-OpenID-Connect--OAuth-2.0/2025.01.1");
  }

  @Test
  void radiantoneSuccess() {
    generateAndBuild(
        "radiantone-openapi-8.1.4-beta.2-SNAPSHOT.yaml", "RadiantOne-V8-API/8.1.4-beta.2-SNAPSHOT");
  }

  @Test
  void githubV3Success() {
    generateAndBuild("github-v3.yaml", "");
  }

  @Test
  void gitlabV4Success() {
    generateAndBuild("gitlab-v4.yaml", "");
  }

  @SneakyThrows
  private void generateAndBuild(
      @NonNull final String yamlFilename, @NonNull final String relativeOutputPath) {
    final Path outputPath = OUTPUT_DIR.resolve(relativeOutputPath);
    FileUtils.deleteDirectory(outputPath.toFile());

    final URL url = getClass().getClassLoader().getResource("openapi/%s".formatted(yamlFilename));
    final Path yamlPath = Paths.get(url.toURI());
    final Runner runner = new Runner();
    final String[] args = new String[] {"-p=%s".formatted(yamlPath.toString())};
    runner.run(args);

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

package com.radiantlogic.dataconnector.restapi.javaclient.builder.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.radiantlogic.dataconnector.restapi.javaclient.builder.Runner;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Disabled;
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
  void oktaIdpMinimal() {
    generateAndBuild("okta-idp-minimal-2025.01.1.yaml", "MyAccount-Management/2025.01.1");
  }

  @Test
  @Disabled(
      "This spec has a lot of problems with it. It is more an example of how poorly designed specs cannot work with this tool than anything else.")
  void oktaManagementMinimal() {
    generateAndBuild("okta-management-minimal-2025.01.1.yaml", "Okta-Admin-Management/2025.01.1");
  }

  @Test
  void oktaOauthMinimal() {
    generateAndBuild(
        "okta-oauth-minimal-2025.01.1.yaml", "Okta-OpenID-Connect--OAuth-2.0/2025.01.1");
  }

  @Test
  void radiantone() {
    generateAndBuild(
        "radiantone-openapi-8.1.4-beta.2-SNAPSHOT.yaml", "RadiantOne-V8-API/8.1.4-beta.2-SNAPSHOT");
  }

  @Test
  void gitlabV4() {
    generateAndBuild("gitlab-v4.yaml", "GitLab-API/v4");
  }

  @Test
  void gitlabV4Swagger2() {
    generateAndBuild("gitlab-v4-swagger2.yaml", "GitLab-API---Swagger-2/v4");
  }

  @Test
  void googleMaps() {
    generateAndBuild("google-maps-1.22.5.json", "Google-Maps-Platform/1.22.5");
  }

  @Test
  void msgraphApplication() {
    generateAndBuild("msgraph-applications-1.0.yaml", "Applications/v1.0");
  }

  @Test
  void msgraphCalendar() {
    generateAndBuild("msgraph-calendar-1.0.yaml", "Calendar/v1.0");
  }

  @Test
  void openai() {
    generateAndBuild("openai-2.3.0.yaml", "OpenAI-API/2.3.0");
  }

  @Test
  void anthropic() {
    generateAndBuild("anthropic-0.0.0.json", "Anthropic-API/0.0.0");
  }

  @Test
  void onepasswordConnect() {
    generateAndBuild("1password-connect-1.7.1.yaml", "1Password-Connect/1.7.1");
  }

  @Test
  void bitbucket() {
    generateAndBuild("bitbucket-2.0.json", "Bitbucket-API/2.0");
  }

  @Test
  void radiantlogicCloudmanager() {
    generateAndBuild("radiantlogic-cloudmanager-1.3.2.json", "Radiantlogic-CloudManager/1.3.2");
  }

  @Test
  void swaggerPetstore() {
    generateAndBuild("swagger-petstore-1.0.26.json", "Swagger-Petstore---OpenAPI-3.0/1.0.26");
  }

  @Test
  void harryPotter() {
    generateAndBuild("harrypotter-1.0.0.yaml", "Harry-Potter-API/1.0.0");
  }

  @Test
  void snyk() {
    generateAndBuild("snyk-1.0.yaml", "Snyk-API/1.0");
  }

  @Test
  void sonarqube() {
    generateAndBuild("sonarqube-2025.4.0.109754.json", "SonarQube-Web-API-v2/2025.4.0.109754");
  }

  @Test
  void githubActions() {
    generateAndBuild("github-actions-v3-1.1.4.yaml", "GitHub-v3-REST-API---actions/1.1.4");
  }

  @Test
  void githubActivity() {
    generateAndBuild("github-activity-v3-1.1.4.yaml", "GitHub-v3-REST-API---activity/1.1.4");
  }

  @Test
  void githubApps() {
    generateAndBuild("github-apps-v3-1.1.4.yaml", "GitHub-v3-REST-API---apps/1.1.4");
  }

  @Test
  void githubBilling() {
    generateAndBuild("github-billing-v3-1.1.4.yaml", "GitHub-v3-REST-API---billing/1.1.4");
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

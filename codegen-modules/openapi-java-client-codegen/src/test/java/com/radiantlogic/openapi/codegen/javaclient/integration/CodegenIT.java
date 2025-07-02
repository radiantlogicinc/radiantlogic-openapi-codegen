package com.radiantlogic.openapi.codegen.javaclient.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.radiantlogic.openapi.codegen.javaclient.Runner;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Integration tests that validate this codegen against various openapi specifications. All of these
 * specifications are real and were acquired off the internet from the companies they are associated
 * with. None have been modified in any way, except in the case of GitHub to split an absurdly huge
 * (to the point of being unparsable) into smaller pieces.
 */
public class CodegenIT {
  private static final Path OUTPUT_DIR = Paths.get(System.getProperty("user.dir"), "output");
  private static final Duration WAIT_FOR_BUILD = Duration.ofMinutes(2);

  private static long peakMemory = 0;

  /**
   * This prints the memory being used on an ongoing basis. This is useful information due to the
   * sheer absurd size of some of the specs.
   */
  @BeforeAll
  static void beforeAll() {
    new Thread(
            () -> {
              while (true) {
                final long amount =
                    Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                if (amount > peakMemory) {
                  peakMemory = amount;
                }
                System.out.printf("Memory Current: %,d Peak: %,d%n", amount, peakMemory);
                try {
                  Thread.sleep(3000);
                } catch (InterruptedException e) {
                }
              }
            })
        .start();
  }

  @Test
  @Disabled // TODO delete this
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
  @Disabled // TODO delete this
  void oktaOauthMinimal() {
    generateAndBuild(
        "okta-oauth-minimal-2025.01.1.yaml", "Okta-OpenID-Connect--OAuth-2.0/2025.01.1");
  }

  @Test
  @Disabled // TODO delete this
  void radiantone() {
    generateAndBuild(
        "radiantone-openapi-8.1.4-beta.2-SNAPSHOT.yaml", "RadiantOne-V8-API/8.1.4-beta.2-SNAPSHOT");
  }

  @Test
  @Disabled // TODO delete this
  void gitlabV4() {
    generateAndBuild("gitlab-v4.yaml", "GitLab-API/v4");
  }

  @Test
  @Disabled("Swagger 2 is currently out of scope")
  void gitlabV4Swagger2() {
    generateAndBuild("gitlab-v4-swagger2.yaml", "GitLab-API---Swagger-2/v4");
  }

  @Test
  @Disabled // TODO delete this
  void googleMaps() {
    generateAndBuild("google-maps-1.22.5.json", "Google-Maps-Platform/1.22.5");
  }

  @Test
  @Disabled // TODO delete this
  void msgraphApplication() {
    generateAndBuild("msgraph-applications-1.0.yaml", "Applications/v1.0");
  }

  @Test
  @Disabled // TODO delete this
  void msgraphCalendar() {
    generateAndBuild("msgraph-calendar-1.0.yaml", "Calendar/v1.0");
  }

  @Test
  @Disabled // TODO delete this
  void openai() {
    generateAndBuild("openai-2.3.0.yaml", "OpenAI-API/2.3.0");
  }

  @Test
  @Disabled // TODO delete this
  void anthropic() {
    generateAndBuild("anthropic-0.0.0.json", "Anthropic-API/0.0.0");
  }

  @Test
  @Disabled // TODO delete this
  void onepasswordConnect() {
    generateAndBuild("1password-connect-1.7.1.yaml", "1Password-Connect/1.7.1");
  }

  @Test
  @Disabled // TODO delete this
  void bitbucket() {
    generateAndBuild("bitbucket-2.0.json", "Bitbucket-API/2.0");
  }

  @Test
  void radiantlogicCloudmanager() {
    generateAndBuild("radiantlogic-cloudmanager-1.3.2.json", "Radiantlogic-CloudManager/1.3.2");
  }

  @Test
  @Disabled // TODO delete this
  void swaggerPetstore() {
    generateAndBuild("swagger-petstore-1.0.26.json", "Swagger-Petstore---OpenAPI-3.0/1.0.26");
  }

  @Test
  @Disabled // TODO delete this
  void harryPotter() {
    generateAndBuild("harrypotter-1.0.0.yaml", "Harry-Potter-API/1.0.0");
  }

  @Test
  @Disabled // TODO delete this
  void snyk() {
    generateAndBuild("snyk-1.0.yaml", "Snyk-API/1.0");
  }

  @Test
  @Disabled // TODO delete this
  void sonarqube() {
    generateAndBuild("sonarqube-2025.4.0.109754.json", "SonarQube-Web-API-v2/2025.4.0.109754");
  }

  @Test
  @Disabled // TODO delete this
  void githubActions() {
    generateAndBuild("github-actions-v3-1.1.4.yaml", "GitHub-v3-REST-API---actions/1.1.4");
  }

  @Test
  @Disabled // TODO delete this
  void githubActivity() {
    generateAndBuild("github-activity-v3-1.1.4.yaml", "GitHub-v3-REST-API---activity/1.1.4");
  }

  @Test
  @Disabled // TODO delete this
  void githubApps() {
    generateAndBuild("github-apps-v3-1.1.4.yaml", "GitHub-v3-REST-API---apps/1.1.4");
  }

  @Test
  @Disabled // TODO delete this
  void githubBilling() {
    generateAndBuild("github-billing-v3-1.1.4.yaml", "GitHub-v3-REST-API---billing/1.1.4");
  }

  /**
   * This generates test code to validate more scenarios involving broken discriminated unions. The
   * existing real specs produce several scenarios, but to fully exercise the impact of the code
   * changes this new test-only spec is required.
   */
  @Test
  @Disabled // TODO delete this
  void brokenDiscriminatorTest() {
    generateAndBuild("broken-discriminator-test-1.0.0.yaml", "Broken-Discriminator-Test/1.0.0");
  }

  @SneakyThrows
  private void generateAndBuild(
      @NonNull final String yamlFilename, @NonNull final String relativeOutputPath) {
    final Path outputPath = OUTPUT_DIR.resolve(relativeOutputPath);
    System.out.printf("Cleaning output directory %s%n", outputPath);
    FileUtils.deleteDirectory(outputPath.toFile());

    final URL url = getClass().getClassLoader().getResource("openapi/%s".formatted(yamlFilename));
    final Path yamlPath = Paths.get(url.toURI());
    System.out.printf("Running codegen for spec file %s%n", yamlPath);
    final Runner runner = new Runner();
    final String[] args = new String[] {"-p=%s".formatted(yamlPath.toString())};
    runner.run(args);

    // TODO delete this block
    System.out.println("OUTPUT EXISTS: " + outputPath + " " + Files.exists(outputPath));
    System.out.println(
        "PARENT EXISTS: " + outputPath.getParent() + " " + Files.exists(outputPath.getParent()));
    Files.list(OUTPUT_DIR).forEach(output -> System.out.println("OUTPUT: " + output));
    runProcess("echo $PATH", outputPath);

    System.out.printf("Codegen complete. Building generated code at %s%n", outputPath);

    final int exitValue = runProcess("mvn clean install -DskipTests", outputPath);

    assertThat(exitValue).isEqualTo(0);
    System.out.println("Build of generated code completed successfully.");
  }

  @SneakyThrows
  private int runProcess(@NonNull final String command, @NonNull final Path directory) {
    final Process process =
        new ProcessBuilder(command.split(" "))
            .directory(directory.toFile())
            .redirectErrorStream(true)
            .start();

    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      reader.lines().forEach(System.out::println);
    }

    final boolean waitSuccess = process.waitFor(WAIT_FOR_BUILD.toMillis(), TimeUnit.MILLISECONDS);
    assertThat(waitSuccess).withFailMessage("Wait for build of generated code timed out.").isTrue();
    return process.exitValue();
  }
}

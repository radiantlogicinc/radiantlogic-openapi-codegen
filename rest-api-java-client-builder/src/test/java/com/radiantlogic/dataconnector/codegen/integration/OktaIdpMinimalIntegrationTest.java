package com.radiantlogic.dataconnector.codegen.integration;

import com.radiantlogic.dataconnector.codegen.Runner;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class OktaIdpMinimalIntegrationTest {
  @Test
  @SneakyThrows
  void itGeneratesAndBuilds() {
    final URL url =
        getClass().getClassLoader().getResource("openapi/okta-idp-minimal-2025.01.1.yaml");
    final Path yamlPath = Paths.get(url.toURI());
    final Runner runner = new Runner();
    final String[] args = new String[] {"-p=%s".formatted(yamlPath.toString())};
    runner.run(args);
  }
}

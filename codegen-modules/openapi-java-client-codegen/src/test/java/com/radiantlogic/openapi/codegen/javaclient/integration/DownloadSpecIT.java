package com.radiantlogic.openapi.codegen.javaclient.integration;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;

/**
 * This test validates that an OpenAPI spec URL that is provided as an argument will be properly
 * resolved and downloaded.
 */
@WireMockTest(httpPort = 9000)
public class DownloadSpecIT {
  @Test
  void itDownloadsSpecUrl() {
    throw new RuntimeException();
  }
}

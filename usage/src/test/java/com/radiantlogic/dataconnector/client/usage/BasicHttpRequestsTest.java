package com.radiantlogic.dataconnector.client.usage;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.radiantlogic.custom.dataconnector.radiantonev8api.invoker.ApiClient;
import org.junit.jupiter.api.BeforeAll;

@WireMockTest(httpPort = 9000)
public class BasicHttpRequestsTest {

  private static ApiClient apiClient;

  @BeforeAll
  static void beforeAll() {
    apiClient = ApiClientSupport.createAndAuthenticateRadiantoneApi();
  }

  //  @Test
  //  void test() {
  //    throw new RuntimeException();
  //  }
}

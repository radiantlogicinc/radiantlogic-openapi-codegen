package com.radiantlogic.dataconnector.client.usage;

import com.radiantlogic.custom.dataconnector.radiantonev8api.invoker.ApiClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@ApiClientTest
public class BasicHttpRequestsTest {

  private static ApiClient apiClient;

  @BeforeAll
  static void beforeAll() {
    apiClient = ApiClientSupport.createAndAuthenticateRadiantoneApi();
  }

  @Test
  void test() {
    throw new RuntimeException();
  }
}

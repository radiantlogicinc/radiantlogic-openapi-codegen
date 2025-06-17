package com.radiantlogic.dataconnector.client.usage;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.radiantlogic.custom.dataconnector.radiantlogiccloudmanager.api.AuthApi;
import com.radiantlogic.custom.dataconnector.radiantlogiccloudmanager.invoker.ApiClient;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@WireMockTest(httpPort = 9000)
public class FormUrlencodedTest {
  private final ObjectMapper objectMapper = new ObjectMapper();
  private static final String BASE_URL = "http://localhost:9000";
  private AuthApi authApi;

  @BeforeEach
  void setUp() {
    final ApiClient apiClient = new ApiClient();
    apiClient.setBasePath(BASE_URL);
    authApi = new AuthApi(apiClient);
  }

  @Test
  void testFormUrlencodedRequest() throws Exception {
    // Setup test data
    final String email = "testuser";
    final String password = "password123";

    // Create expected response
    final Map<String, Object> expectedResponse = new HashMap<>();
    expectedResponse.put("access_token", "test-token");
    expectedResponse.put("token_type", "bearer");
    expectedResponse.put("expires_in", 3600);

    // Setup WireMock stub for the form-urlencoded request
    stubFor(
        post(urlPathEqualTo("/eoc-backend/auth/local"))
            .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded;charset=UTF-8"))
            .withFormParam("email", equalTo(email))
            .withFormParam("password", equalTo(password))
            .willReturn(
                aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(expectedResponse))));

    // Make the request using the AuthApi client
    final Object result = authApi.authControllerLocalLogin(email, password, null);

    // Verify the result
    assertThat(result).isNotNull();
    assertThat(result).isInstanceOf(Map.class);
    final Map<String, Object> resultMap = (Map<String, Object>) result;

    // Compare using recursive comparator
    assertThat(resultMap).usingRecursiveComparison().isEqualTo(expectedResponse);

    // Verify the request was made with the correct form parameters
    verify(
        postRequestedFor(urlPathEqualTo("/eoc-backend/auth/local"))
            .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded;charset=UTF-8"))
            .withFormParam("email", equalTo(email))
            .withFormParam("password", equalTo(password)));
  }
}

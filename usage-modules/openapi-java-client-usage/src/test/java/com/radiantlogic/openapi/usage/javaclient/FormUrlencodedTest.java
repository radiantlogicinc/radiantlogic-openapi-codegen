package com.radiantlogic.openapi.usage.javaclient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@WireMockTest(httpPort = 9000)
public class FormUrlencodedTest {
  private final ObjectMapper objectMapper = new ObjectMapper();

  //  private AuthApi authApi;

  @BeforeEach
  void setUp() {
    //    final ApiClient apiClient = new ApiClient();
    //    apiClient.setBasePath(BASE_URL);
    //    apiClient.setDebugging(true);
    //    authApi = new AuthApi(apiClient);
  }

  @Test
  void testFormUrlencodedRequest() throws Exception {
    final String email = "testuser";
    final String password = "password123";

    final Map<String, Object> expectedResponse = new HashMap<>();
    expectedResponse.put("access_token", "test-token");
    expectedResponse.put("token_type", "bearer");
    expectedResponse.put("expires_in", 3600);

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

    //    final Object result = authApi.authControllerLocalLogin(email, password, null);
    //
    //    assertThat(result).isNotNull();
    //    assertThat(result).isInstanceOf(Map.class);
    //    final Map<String, Object> resultMap = (Map<String, Object>) result;
    //
    //    assertThat(resultMap).usingRecursiveComparison().isEqualTo(expectedResponse);

    verify(
        postRequestedFor(urlPathEqualTo("/eoc-backend/auth/local"))
            .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded;charset=UTF-8"))
            .withFormParam("email", equalTo(email))
            .withFormParam("password", equalTo(password)));
  }
}

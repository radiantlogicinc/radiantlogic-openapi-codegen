package com.radiantlogic.openapi.usage.javaclient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.radiantlogic.openapi.generated.oktaopenidconnectoauth20.api.OrgAsApi;
import com.radiantlogic.openapi.generated.oktaopenidconnectoauth20.invoker.ApiClient;
import com.radiantlogic.openapi.generated.oktaopenidconnectoauth20.model.AcrValue;
import com.radiantlogic.openapi.generated.oktaopenidconnectoauth20.model.AmrValue;
import com.radiantlogic.openapi.generated.oktaopenidconnectoauth20.model.CodeChallengeMethod;
import com.radiantlogic.openapi.generated.oktaopenidconnectoauth20.model.Prompt;
import com.radiantlogic.openapi.generated.oktaopenidconnectoauth20.model.ResponseMode;
import com.radiantlogic.openapi.generated.oktaopenidconnectoauth20.model.ResponseTypesSupported;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@WireMockTest(httpPort = 9000)
public class FormUrlencodedTest {
  private final ObjectMapper objectMapper = new ObjectMapper();
  private OrgAsApi orgAsApi;

  @BeforeEach
  void setUp() {
    final ApiClient apiClient = ApiClientSupport.createOktaOpenidConnectApiClient();
    orgAsApi = new OrgAsApi(apiClient);
  }

  @Test
  void testFormUrlencodedRequest() throws Exception {
    final String clientId = "client-id";
    final String codeChallenge = "challenge";
    final CodeChallengeMethod codeChallengeMethod = CodeChallengeMethod.S256;
    final ResponseTypesSupported responseTypesSupported = ResponseTypesSupported.CODE;
    final AcrValue acrValue = AcrValue.PHR;
    final AmrValue amrValue = AmrValue.DUO;

    final Map<String, Object> expectedResponse = new HashMap<>();
    expectedResponse.put("access_token", "test-token");
    expectedResponse.put("token_type", "bearer");
    expectedResponse.put("expires_in", 3600);

    stubFor(
        post(urlPathEqualTo("/oauth2/v1/authorize"))
            .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded;charset=UTF-8"))
            .withFormParam("client_id", equalTo(clientId))
            .withFormParam("code_challenge", equalTo(codeChallenge))
            .withFormParam("code_challenge_method", equalTo(codeChallengeMethod.getValue()))
            .withFormParam("response_type", equalTo(responseTypesSupported.getValue()))
            .withFormParam("acr_values", equalTo(acrValue.getValue()))
            .withFormParam("enroll_amr_values", equalTo(amrValue.getValue()))
            .willReturn(
                aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(expectedResponse))));

    // If it doesn't match the stub, an exception will be thrown
    orgAsApi.authorizeWithPost(
        clientId,
        "",
        responseTypesSupported,
        "",
        "",
        acrValue,
        codeChallenge,
        codeChallengeMethod,
        "",
        amrValue,
        "",
        "",
        "",
        10,
        "",
        Prompt.NONE,
        "",
        "",
        ResponseMode.FORM_POST,
        "");
  }
}

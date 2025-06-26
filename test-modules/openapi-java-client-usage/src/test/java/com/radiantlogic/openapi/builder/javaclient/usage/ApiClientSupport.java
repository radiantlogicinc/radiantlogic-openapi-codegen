package com.radiantlogic.openapi.builder.javaclient.usage;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.radiantlogic.custom.dataconnector.radiantonev8api.api.AuthTokenApiApi;
import com.radiantlogic.custom.dataconnector.radiantonev8api.model.PostLogin200Response;
import java.util.Base64;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

/** A support class that produces pre-authenticated API clients for testing. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiClientSupport {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static final String BASE_URL = "http://localhost:9000";
  public static final String USERNAME = "user";
  public static final String PASSWORD = "password";
  public static final String ACCESS_TOKEN = "access_token";
  public static final String GITLAB_PRIVATE_TOKEN = "gitlab_private_token";

  @SneakyThrows
  public static com.radiantlogic.custom.dataconnector.radiantonev8api.invoker.ApiClient
      createAndAuthenticateRadiantoneApi() {
    final String basicAuth = String.format("%s:%s", USERNAME, PASSWORD);
    final String base64EncodedAuth = Base64.getEncoder().encodeToString(basicAuth.getBytes());

    final PostLogin200Response responseBody = new PostLogin200Response();
    responseBody.setToken(ACCESS_TOKEN);

    final ResponseDefinitionBuilder response =
        aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(responseBody));
    final MappingBuilder mapping =
        post(urlEqualTo("/authentication-service/login"))
            .withBasicAuth(USERNAME, PASSWORD)
            .willReturn(response);
    stubFor(mapping);

    final com.radiantlogic.custom.dataconnector.radiantonev8api.invoker.ApiClient apiClient =
        new com.radiantlogic.custom.dataconnector.radiantonev8api.invoker.ApiClient();
    apiClient.setDebugging(true);
    apiClient.setBasePath(BASE_URL);

    final AuthTokenApiApi authTokenApiApi = new AuthTokenApiApi(apiClient);
    final String token =
        authTokenApiApi.postLogin(String.format("Basic %s", base64EncodedAuth)).getToken();

    apiClient.setBearerToken(token);

    return apiClient;
  }

  @SneakyThrows
  public static com.radiantlogic.custom.dataconnector.gitlabapi.invoker.ApiClient
      createAndAuthenticateGitlabApi() {
    final com.radiantlogic.custom.dataconnector.gitlabapi.invoker.ApiClient apiClient =
        new com.radiantlogic.custom.dataconnector.gitlabapi.invoker.ApiClient();
    apiClient.setDebugging(true);
    apiClient.setBasePath(BASE_URL);

    // GitLab API uses a private token for authentication
    apiClient.addDefaultHeader("PRIVATE-TOKEN", GITLAB_PRIVATE_TOKEN);

    return apiClient;
  }

  public static com.radiantlogic.custom.dataconnector.openaiapi.invoker.ApiClient
      createAndAuthenticateOpenAIApiClient() {
    final com.radiantlogic.custom.dataconnector.openaiapi.invoker.ApiClient apiClient =
        new com.radiantlogic.custom.dataconnector.openaiapi.invoker.ApiClient();
    apiClient.setDebugging(true);
    apiClient.setBasePath(ApiClientSupport.BASE_URL);

    apiClient.setBearerToken(ACCESS_TOKEN);

    return apiClient;
  }
}

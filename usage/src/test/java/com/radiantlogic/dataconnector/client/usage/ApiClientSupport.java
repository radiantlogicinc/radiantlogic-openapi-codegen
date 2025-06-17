package com.radiantlogic.dataconnector.client.usage;

import com.radiantlogic.custom.dataconnector.radiantonev8api.api.AuthTokenApiApi;
import java.util.Base64;

public class ApiClientSupport {
  private static final String BASE_URL = "http://localhost:9000";
  private static final String USERNAME = "user";
  private static final String PASSWORD = "password";

  public com.radiantlogic.custom.dataconnector.radiantonev8api.invoker.ApiClient
      createAndAuthenticateRadiantoneApi() {
    final com.radiantlogic.custom.dataconnector.radiantonev8api.invoker.ApiClient apiClient =
        new com.radiantlogic.custom.dataconnector.radiantonev8api.invoker.ApiClient();
    apiClient.setDebugging(true);
    apiClient.setBasePath(BASE_URL);

    final AuthTokenApiApi authTokenApiApi = new AuthTokenApiApi(apiClient);
    final String basicAuth = String.format("%s:%s", USERNAME, PASSWORD);
    final String base64EncodedAuth = Base64.getEncoder().encodeToString(basicAuth.getBytes());
    final String token =
        authTokenApiApi.postLogin(String.format("Basic %s", base64EncodedAuth)).getToken();

    apiClient.setBearerToken(token);

    return apiClient;
  }
}

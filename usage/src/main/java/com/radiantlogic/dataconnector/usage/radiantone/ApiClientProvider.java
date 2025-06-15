package com.radiantlogic.dataconnector.usage.radiantone;

import com.radiantlogic.custom.dataconnector.radiantonev8api.api.AuthTokenApiApi;
import com.radiantlogic.custom.dataconnector.radiantonev8api.invoker.ApiClient;

import java.util.Base64;

public class ApiClientProvider {
    public ApiClient createAndPrepareApiClient(final String basePath, final String username, final String password) {
        final ApiClient apiClient = new ApiClient();
        apiClient.setDebugging(true);
        apiClient.setBasePath(basePath);

        final AuthTokenApiApi authTokenApiApi = new AuthTokenApiApi(apiClient);
        final String basicAuth = String.format("%s:%s", username, password);
        final String base64EncodedAuth = Base64.getEncoder().encodeToString(basicAuth.getBytes());
        final String token =
                authTokenApiApi.postLogin(String.format("Basic %s", base64EncodedAuth)).getToken();

        apiClient.setBearerToken(token);

        return apiClient;
    }
}

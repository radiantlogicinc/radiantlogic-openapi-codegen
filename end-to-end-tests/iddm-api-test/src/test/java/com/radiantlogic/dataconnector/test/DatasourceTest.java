package com.radiantlogic.dataconnector.test;

import com.radiantlogic.custom.dataconnector.api.AuthTokenApiApi;
import com.radiantlogic.custom.dataconnector.invoker.ApiClient;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class DatasourceTest {
    @Test
    void itCanCreateAndReadDatasource() {
        final ApiClient apiClient = new ApiClient();
        apiClient.setBasePath("https://rlqa-usw2-craig.dev01.radiantlogic.io");

        final AuthTokenApiApi authTokenApiApi = new AuthTokenApiApi(apiClient);
        final String username = System.getenv("IDDM_USERNAME");
        final String password = System.getenv("IDDM_PASSWORD");
        authTokenApiApi.postLogin(Base64.getEncoder().encodeToString("%s:%s".formatted(username, password).getBytes(StandardCharsets.UTF_8))).getToken();

        apiClient.setBearerToken(() -> {
            System.getenv()
            return authTokenApiApi.postLogin().getToken();
        });
    }
}

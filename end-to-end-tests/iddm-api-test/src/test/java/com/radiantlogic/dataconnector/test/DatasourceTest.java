package com.radiantlogic.dataconnector.test;

import com.radiantlogic.custom.dataconnector.api.AuthTokenApiApi;
import com.radiantlogic.custom.dataconnector.invoker.ApiClient;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class DatasourceTest {
  @Test
  @SneakyThrows
  void itCanCreateAndReadDatasource() {
    final ApiClient apiClient = new ApiClient();
    apiClient.setBasePath("https://rlqa-usw2-craig.dev01.radiantlogic.io");

    final AuthTokenApiApi authTokenApiApi = new AuthTokenApiApi(apiClient);
    final String username = System.getenv("IDDM_USERNAME");
    final String password = System.getenv("IDDM_PASSWORD");
    final String auth =
        Base64.getEncoder()
            .encodeToString(
                String.format("%s:%s", username, password).getBytes(StandardCharsets.UTF_8));

    final String token = authTokenApiApi.postLogin(auth).getToken();
    apiClient.setBearerToken(token);
  }
}

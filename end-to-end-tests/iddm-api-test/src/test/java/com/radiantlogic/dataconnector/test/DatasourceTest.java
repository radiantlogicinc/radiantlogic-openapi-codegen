package com.radiantlogic.dataconnector.test;

import com.radiantlogic.custom.dataconnector.api.AuthTokenApiApi;
import com.radiantlogic.custom.dataconnector.invoker.ApiClient;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class DatasourceTest {
  @Test
  @SneakyThrows
  void itCanCreateAndReadDatasource() {
    final Properties testProps = new Properties();
    try (InputStream stream = getClass().getClassLoader().getResourceAsStream("test.properties")) {
      testProps.load(stream);
    }

    final ApiClient apiClient = new ApiClient();
    apiClient.setBasePath(testProps.getProperty("basePath"));
    apiClient.setLenientOnJson(true);

    final AuthTokenApiApi authTokenApiApi = new AuthTokenApiApi(apiClient);
    final String username = testProps.getProperty("username");
    final String password = testProps.getProperty("password");
    final String auth =
        Base64.getEncoder()
            .encodeToString(
                String.format("%s:%s", username, password).getBytes(StandardCharsets.UTF_8));

    final String token = authTokenApiApi.postLogin(String.format("Basic %s", auth)).getToken();
    apiClient.setBearerToken(token);
  }
}

package com.radiantlogic.dataconnector.oktaapi;

import com.radiantlogic.custom.dataconnector.api.EmailApi;
import com.radiantlogic.custom.dataconnector.invoker.ApiClient;
import com.radiantlogic.custom.dataconnector.model.Email;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class EmailsTest {
  @Test
  @SneakyThrows
  void itCanGetEmails() {
    final ApiClient apiClient = new ApiClient();

    final EmailApi emailApi = new EmailApi(apiClient);
    final Email email = emailApi.getEmail("1");
  }
}

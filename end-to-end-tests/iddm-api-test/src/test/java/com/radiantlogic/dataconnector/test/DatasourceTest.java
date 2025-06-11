package com.radiantlogic.dataconnector.test;

import com.radiantlogic.custom.dataconnector.invoker.ApiClient;
import org.junit.jupiter.api.Test;

public class DatasourceTest {
    @Test
    void itCanCreateAndReadDatasource() {
        final ApiClient apiClient = new ApiClient();
        apiClient.setBasePath("https://rlqa-usw2-craig.dev01.radiantlogic.io");
    }
}

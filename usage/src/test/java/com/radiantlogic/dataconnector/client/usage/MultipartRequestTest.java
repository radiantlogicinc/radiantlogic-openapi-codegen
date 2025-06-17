package com.radiantlogic.dataconnector.client.usage;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.radiantlogic.dataconnector.client.usage.ApiClientSupport.GITLAB_PRIVATE_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.matching.MultipartValuePattern;
import com.radiantlogic.custom.dataconnector.gitlabapi.api.AlertManagementApi;
import com.radiantlogic.custom.dataconnector.gitlabapi.invoker.ApiClient;
import com.radiantlogic.custom.dataconnector.gitlabapi.model.APIEntitiesMetricImage;
import java.io.File;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@WireMockTest(httpPort = 9000)
public class MultipartRequestTest {
  private static ApiClient apiClient;
  private AlertManagementApi alertManagementApi;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @TempDir Path tempDir;

  @BeforeAll
  static void beforeAll() {
    apiClient = ApiClientSupport.createAndAuthenticateGitlabApi();
  }

  @BeforeEach
  void setUp() {
    alertManagementApi = new AlertManagementApi(apiClient);
  }

  @Test
  void testPostApiV4ProjectsIdAlertManagementAlertsAlertIidMetricImages() throws Exception {
    final File imageFile = ResourceReader.getFilePath("data/ai-generated-image.png").toFile();

    final String projectId = "123";
    final Integer alertIid = 456;
    final String url = "https://example.com/metrics";
    final String urlText = "Example Metrics";

    final APIEntitiesMetricImage expectedResponse = new APIEntitiesMetricImage();
    expectedResponse.setId(789);
    expectedResponse.setUrl(url);
    expectedResponse.setUrlText(urlText);
    expectedResponse.setFilename("test-image.png");

    stubFor(
        post(urlPathEqualTo("/projects/123/alert_management_alerts/456/metric_images"))
            .withHeader("PRIVATE-TOKEN", equalTo(GITLAB_PRIVATE_TOKEN))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(expectedResponse))));

    final APIEntitiesMetricImage result =
        alertManagementApi.postApiV4ProjectsIdAlertManagementAlertsAlertIidMetricImages(
            projectId, alertIid, imageFile, url, urlText);

    assertThat(result).usingRecursiveComparison().isEqualTo(expectedResponse);

    final byte[] imageBytes = ResourceReader.readBytes("data/ai-generated-image.png");

    final MultipartValuePattern filePart =
        aMultipart("file")
            .withHeader(
                "Content-Disposition",
                equalTo("form-data; name=\"file\"; filename=\"ai-generated-image.png\""))
            .withHeader("Content-Type", equalTo("image/png"))
            .withHeader("Content-Length", equalTo("33"))
            .withBody(binaryEqualTo(imageBytes))
            .build();

    final MultipartValuePattern urlPart =
        aMultipart("url")
            .withHeader("Content-Disposition", equalTo("form-data; name=\"url\""))
            .withHeader("Content-Type", equalTo("text/plain;charset=UTF-8"))
            .withHeader("Content-Length", equalTo("27"))
            .withBody(equalTo(url))
            .build();

    final MultipartValuePattern urlTextPart =
        aMultipart("url_text")
            .withHeader("Content-Disposition", equalTo("form-data; name=\"url_text\""))
            .withHeader("Content-Type", equalTo("text/plain;charset=UTF-8"))
            .withHeader("Content-Length", equalTo("15"))
            .withBody(equalTo(urlText))
            .build();

    verify(
        postRequestedFor(urlPathEqualTo("/projects/123/alert_management_alerts/456/metric_images"))
            .withHeader("PRIVATE-TOKEN", equalTo(GITLAB_PRIVATE_TOKEN))
            .withHeader("Content-Type", containing("multipart/form-data"))
            .withRequestBodyPart(filePart)
            .withRequestBodyPart(urlPart)
            .withRequestBodyPart(urlTextPart));
  }
}

package com.radiantlogic.dataconnector.client.usage;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.radiantlogic.dataconnector.client.usage.ApiClientSupport.GITLAB_PRIVATE_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.radiantlogic.custom.dataconnector.gitlabapi.api.AlertManagementApi;
import com.radiantlogic.custom.dataconnector.gitlabapi.invoker.ApiClient;
import com.radiantlogic.custom.dataconnector.gitlabapi.model.APIEntitiesMetricImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
    // Create a temporary image file for testing
    File imageFile = createTempImageFile();

    // Set up test parameters
    String projectId = "123";
    Integer alertIid = 456;
    String url = "https://example.com/metrics";
    String urlText = "Example Metrics";

    // Create expected response
    APIEntitiesMetricImage expectedResponse = new APIEntitiesMetricImage();
    expectedResponse.setId(789);
    expectedResponse.setUrl(url);
    expectedResponse.setUrlText(urlText);
    expectedResponse.setFilename("test-image.png");

    // Set up WireMock stub for the multipart request
    stubFor(
        post(urlPathEqualTo("/projects/123/alert_management_alerts/456/metric_images"))
            .withHeader("PRIVATE-TOKEN", equalTo(GITLAB_PRIVATE_TOKEN))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(expectedResponse))));

    // Call the API method
    APIEntitiesMetricImage result =
        alertManagementApi.postApiV4ProjectsIdAlertManagementAlertsAlertIidMetricImages(
            projectId, alertIid, imageFile, url, urlText);

    // Verify the result
    assertThat(result).usingRecursiveComparison().isEqualTo(expectedResponse);

    // Verify that the request was made with the correct parameters
    verify(
        postRequestedFor(urlPathEqualTo("/projects/123/alert_management_alerts/456/metric_images"))
            .withHeader("PRIVATE-TOKEN", equalTo(GITLAB_PRIVATE_TOKEN))
            .withHeader("Content-Type", containing("multipart/form-data")));
  }

  private File createTempImageFile() throws IOException {
    // Create a simple PNG file for testing
    Path imagePath = tempDir.resolve("test-image.png");
    byte[] imageData =
        new byte[] {
          (byte) 0x89,
          'P',
          'N',
          'G',
          '\r',
          '\n',
          0x1a,
          '\n', // PNG signature
          0,
          0,
          0,
          13, // IHDR chunk length
          'I',
          'H',
          'D',
          'R', // IHDR chunk type
          0,
          0,
          0,
          1, // width: 1 pixel
          0,
          0,
          0,
          1, // height: 1 pixel
          8, // bit depth: 8 bits per channel
          6, // color type: RGBA
          0, // compression method: deflate
          0, // filter method: standard
          0, // interlace method: no interlace
          0,
          0,
          0,
          0 // CRC (not correct, but sufficient for testing)
        };
    Files.write(imagePath, imageData);
    return imagePath.toFile();
  }
}

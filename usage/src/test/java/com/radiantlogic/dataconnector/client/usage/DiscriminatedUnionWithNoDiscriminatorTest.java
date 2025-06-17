package com.radiantlogic.dataconnector.client.usage;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.radiantlogic.custom.dataconnector.openaiapi.api.ResponsesApi;
import com.radiantlogic.custom.dataconnector.openaiapi.invoker.ApiClient;
import com.radiantlogic.custom.dataconnector.openaiapi.model.FunctionToolCallResource;
import com.radiantlogic.custom.dataconnector.openaiapi.model.InputMessageResource;
import com.radiantlogic.custom.dataconnector.openaiapi.model.InputTextContent;
import com.radiantlogic.custom.dataconnector.openaiapi.model.ResponseItemList;
import com.radiantlogic.custom.dataconnector.openaiapi.model.RoleEnum;
import com.radiantlogic.custom.dataconnector.openaiapi.model.StatusEnum;
import com.radiantlogic.custom.dataconnector.openaiapi.model.TypeEnum;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@WireMockTest(httpPort = 9000)
public class DiscriminatedUnionWithNoDiscriminatorTest {
  private static ApiClient apiClient;
  private ResponsesApi responsesApi;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeAll
  static void beforeAll() {
    apiClient = createOpenAIApiClient();
  }

  @BeforeEach
  void setUp() {
    responsesApi = new ResponsesApi(apiClient);
  }

  /**
   * Creates and configures an OpenAI API client for testing.
   *
   * @return A configured OpenAI API client
   */
  private static ApiClient createOpenAIApiClient() {
    final ApiClient apiClient = new ApiClient();
    apiClient.setDebugging(true);
    apiClient.setBasePath(ApiClientSupport.BASE_URL);

    // OpenAI API uses an API key for authentication
    apiClient.addDefaultHeader("Authorization", "Bearer test-api-key");

    return apiClient;
  }

  @Test
  void testListInputItems() {
    final String responseId = "resp_123456789";

    // Create a response using Java objects instead of literal JSON
    // This will be used to test if the discriminator works correctly
    ResponseItemList responseItemList = new ResponseItemList();
    responseItemList.setObject(ResponseItemList.ObjectEnum.LIST);
    responseItemList.setHasMore(false);
    responseItemList.setFirstId("item_1");
    responseItemList.setLastId("item_2");

    // Create a message item
    InputMessageResource messageItem = new InputMessageResource();
    messageItem.setType(TypeEnum.MESSAGE);
    messageItem.setRole(RoleEnum.USER);
    messageItem.setId("item_1");
    messageItem.setStatus(StatusEnum.COMPLETED);

    // Create content for the message
    InputTextContent textContent = new InputTextContent();
    textContent.setText("Hello, world!");
    messageItem.addContentItem(textContent);

    // Create a function call item
    FunctionToolCallResource functionCallItem = new FunctionToolCallResource();
    functionCallItem.setType(TypeEnum.FUNCTION_CALL);
    functionCallItem.setId("item_2");
    functionCallItem.setCallId("call_123");
    functionCallItem.setName("get_weather");
    functionCallItem.setArguments("{\"location\": \"San Francisco\", \"unit\": \"celsius\"}");
    functionCallItem.setStatus(StatusEnum.COMPLETED);

    // Add items to the response
    responseItemList.addDataItem(messageItem);
    responseItemList.addDataItem(functionCallItem);

    // Serialize the response to JSON
    String jsonResponse;
    try {
      jsonResponse = objectMapper.writeValueAsString(responseItemList);
    } catch (Exception e) {
      throw new RuntimeException("Failed to serialize response", e);
    }

    // Set up the mock response
    stubFor(
        get(urlPathEqualTo("/responses/" + responseId + "/input_items"))
            .withHeader("Authorization", equalTo("Bearer test-api-key"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(jsonResponse)));

    try {
      // Call the API - this should fail due to the missing discriminator mapping
      responsesApi.listInputItems(responseId, null, null, null, null, null);

      // If we get here, the test has failed because the API call should throw an exception
      assertThat(false).as("Expected exception was not thrown").isTrue();
    } catch (Exception e) {
      // Verify that the exception is the expected one
      assertThat(e).isInstanceOf(org.springframework.web.client.RestClientException.class);

      // Verify that the exception message contains the expected text about the missing type id
      // mapping
      String errorMessage = e.getMessage();
      assertThat(errorMessage).contains("Could not resolve type id 'message' as a subtype of");
      assertThat(errorMessage)
          .contains("com.radiantlogic.custom.dataconnector.openaiapi.model.ItemResource");
      assertThat(errorMessage).contains("known type ids = []");
    }
  }
}

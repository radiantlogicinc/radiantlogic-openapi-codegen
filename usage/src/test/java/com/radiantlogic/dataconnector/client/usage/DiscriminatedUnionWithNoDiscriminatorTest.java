package com.radiantlogic.dataconnector.client.usage;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.radiantlogic.dataconnector.client.usage.ApiClientSupport.ACCESS_TOKEN;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
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
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.client.RestClientException;

/**
 * This test validates a specific scenario in one of the test specs. That spec contains a
 * discriminated union type but does not define a discriminator mapping. The result is the generated
 * code won't be able to de-serialize the response correctly.
 */
@WireMockTest(httpPort = 9000)
public class DiscriminatedUnionWithNoDiscriminatorTest {
  private static ApiClient apiClient;
  private ResponsesApi responsesApi;
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeAll
  static void beforeAll() {
    apiClient = ApiClientSupport.createAndAuthenticateOpenAIApiClient();
  }

  @BeforeEach
  void setUp() {
    responsesApi = new ResponsesApi(apiClient);
  }

  @Test
  @SneakyThrows
  void testListInputItems() {
    final String responseId = "resp_123456789";
    final ResponseItemList responseItemList = new ResponseItemList();
    responseItemList.setObject(ResponseItemList.ObjectEnum.LIST);
    responseItemList.setHasMore(false);
    responseItemList.setFirstId("item_1");
    responseItemList.setLastId("item_2");

    final InputMessageResource messageItem = new InputMessageResource();
    messageItem.setType(TypeEnum.MESSAGE);
    messageItem.setRole(RoleEnum.USER);
    messageItem.setId("item_1");
    messageItem.setStatus(StatusEnum.COMPLETED);

    final InputTextContent textContent = new InputTextContent();
    textContent.setText("Hello, world!");
    messageItem.addContentItem(textContent);

    final FunctionToolCallResource functionCallItem = new FunctionToolCallResource();
    functionCallItem.setType(TypeEnum.FUNCTION_CALL);
    functionCallItem.setId("item_2");
    functionCallItem.setCallId("call_123");
    functionCallItem.setName("get_weather");
    functionCallItem.setArguments("{\"location\": \"San Francisco\", \"unit\": \"celsius\"}");
    functionCallItem.setStatus(StatusEnum.COMPLETED);

    responseItemList.addDataItem(messageItem);
    responseItemList.addDataItem(functionCallItem);

    final String jsonResponse = objectMapper.writeValueAsString(responseItemList);

    stubFor(
        get(urlPathEqualTo(String.format("/responses/%s/input_items", responseId)))
            .withHeader("Authorization", equalTo(String.format("Bearer %s", ACCESS_TOKEN)))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(jsonResponse)));

    assertThatThrownBy(() -> responsesApi.listInputItems(responseId, null, null, null, null, null))
        .isInstanceOf(RestClientException.class)
        .extracting("cause")
        .isNotNull()
        .isInstanceOf(HttpMessageNotReadableException.class)
        .extracting("cause")
        .isNotNull()
        .isInstanceOf(InvalidTypeIdException.class);
  }
}

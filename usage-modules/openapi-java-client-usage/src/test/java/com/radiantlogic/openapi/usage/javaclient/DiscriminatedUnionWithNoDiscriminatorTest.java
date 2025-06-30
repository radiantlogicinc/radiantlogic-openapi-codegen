package com.radiantlogic.openapi.usage.javaclient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.radiantlogic.openapi.usage.javaclient.ApiClientSupport.ACCESS_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
import com.radiantlogic.openapi.generated.brokendiscriminatortest.api.UnionSerdeApi;
import com.radiantlogic.openapi.generated.brokendiscriminatortest.model.BrokenDiscriminatedUnion;
import com.radiantlogic.openapi.generated.brokendiscriminatortest.model.Discriminator;
import com.radiantlogic.openapi.generated.brokendiscriminatortest.model.FirstChild;
import com.radiantlogic.openapi.generated.brokendiscriminatortest.model.SecondChild;
import com.radiantlogic.openapi.generated.brokendiscriminatortest.model.ThirdChild;
import com.radiantlogic.openapi.generated.openaiapi.api.ResponsesApi;
import com.radiantlogic.openapi.generated.openaiapi.invoker.ApiClient;
import com.radiantlogic.openapi.generated.openaiapi.model.FunctionToolCallResource;
import com.radiantlogic.openapi.generated.openaiapi.model.InputMessageResource;
import com.radiantlogic.openapi.generated.openaiapi.model.InputTextContent;
import com.radiantlogic.openapi.generated.openaiapi.model.ResponseItemList;
import com.radiantlogic.openapi.generated.openaiapi.model.RoleEnum;
import com.radiantlogic.openapi.generated.openaiapi.model.StatusEnum;
import com.radiantlogic.openapi.generated.openaiapi.model.TypeEnum;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

/**
 * This test validates a specific scenario in one of the test specs. That spec contains a
 * discriminated union type but does not define a discriminator mapping. The result is the generated
 * code won't be able to de-serialize the response correctly.
 */
@WireMockTest(httpPort = 9000)
public class DiscriminatedUnionWithNoDiscriminatorTest {
  private static ApiClient openaiApiClient;
  private static com.radiantlogic.openapi.generated.brokendiscriminatortest.invoker.ApiClient
      brokenDiscriminatorApiClient;
  private UnionSerdeApi unionSerdeApi;
  private ResponsesApi responsesApi;
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeAll
  static void beforeAll() {
    openaiApiClient = ApiClientSupport.createAndAuthenticateOpenAIApiClient();
    brokenDiscriminatorApiClient = ApiClientSupport.createBrokenDiscriminatorApiClient();
  }

  @BeforeEach
  void setUp() {
    responsesApi = new ResponsesApi(openaiApiClient);
    unionSerdeApi = new UnionSerdeApi(brokenDiscriminatorApiClient);
  }

  private static List<BrokenDiscriminatedUnion> buildBrokenUnionList() {
    final FirstChild firstChild = new FirstChild().first("The First").type(Discriminator.FIRST);
    final SecondChild secondChild =
        new SecondChild().second("The Second").type(Discriminator.SECOND);
    final ThirdChild thirdChild = new ThirdChild().third("The Third").type(Discriminator.THIRD);
    return Arrays.asList(firstChild, secondChild, thirdChild);
  }

  private static ResponseItemList buildResponseItemList() {
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
    messageItem.addContentItem(textContent.toInputContentRaw());

    final FunctionToolCallResource functionCallItem = new FunctionToolCallResource();
    functionCallItem.setType(TypeEnum.FUNCTION_CALL);
    functionCallItem.setId("item_2");
    functionCallItem.setCallId("call_123");
    functionCallItem.setName("get_weather");
    functionCallItem.setArguments("args");
    functionCallItem.setStatus(StatusEnum.COMPLETED);

    responseItemList.addDataItem(messageItem.toItemResourceRaw());
    responseItemList.addDataItem(functionCallItem.toItemResourceRaw());
    return responseItemList;
  }

  @Test
  @SneakyThrows
  void itRetrievesDiscriminatedUnion() {
    final String responseId = "resp_123456789";
    final ResponseItemList responseItemList = buildResponseItemList();

    final String jsonResponse =
        ResourceReader.readString("data/discriminatedunionnodiscriminator/responseitemlist.json");

    stubFor(
        get(urlPathEqualTo(String.format("/responses/%s/input_items", responseId)))
            .withHeader("Authorization", equalTo(String.format("Bearer %s", ACCESS_TOKEN)))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(jsonResponse)));

    final ResponseItemList response =
        responsesApi.listInputItems(responseId, null, null, null, null, null);
    assertThat(response).usingRecursiveComparison().isEqualTo(responseItemList);

    assertThat(response.getData().get(0).getType()).isEqualTo(TypeEnum.MESSAGE);
    assertThat(response.getData().get(0).toImplementation(InputMessageResource.class))
        .isEqualTo(responseItemList.getData().get(0).toImplementation(InputMessageResource.class));
    assertThat(response.getData().get(1).getType()).isEqualTo(TypeEnum.FUNCTION_CALL);
    assertThat(response.getData().get(1).toImplementation(FunctionToolCallResource.class))
        .isEqualTo(
            responseItemList.getData().get(1).toImplementation(FunctionToolCallResource.class));

    verify(
        getRequestedFor(urlPathEqualTo(String.format("/responses/%s/input_items", responseId)))
            .withHeader("Authorization", equalTo(String.format("Bearer %s", ACCESS_TOKEN))));
  }

  @Test
  @SneakyThrows
  void itRetrievesDiscriminatedUnionWithHttpInfo() {
    final String responseId = "resp_123456789";
    final ResponseItemList responseItemList = buildResponseItemList();
    final String jsonResponse =
        ResourceReader.readString("data/discriminatedunionnodiscriminator/responseitemlist.json");

    stubFor(
        get(urlPathEqualTo(String.format("/responses/%s/input_items", responseId)))
            .withHeader("Authorization", equalTo(String.format("Bearer %s", ACCESS_TOKEN)))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(jsonResponse)));

    final ResponseEntity<ResponseItemList> responseEntity =
        responsesApi.listInputItemsWithHttpInfo(responseId, null, null, null, null, null);
    final ResponseItemList response = responseEntity.getBody();

    assertThat(response).usingRecursiveComparison().isEqualTo(responseItemList);

    assertThat(response.getData().get(0).getType()).isEqualTo(TypeEnum.MESSAGE);
    assertThat(response.getData().get(0).toImplementation(InputMessageResource.class))
        .isEqualTo(responseItemList.getData().get(0).toImplementation(InputMessageResource.class));
    assertThat(response.getData().get(1).getType()).isEqualTo(TypeEnum.FUNCTION_CALL);
    assertThat(response.getData().get(1).toImplementation(FunctionToolCallResource.class))
        .isEqualTo(
            responseItemList.getData().get(1).toImplementation(FunctionToolCallResource.class));

    verify(
        getRequestedFor(urlPathEqualTo(String.format("/responses/%s/input_items", responseId)))
            .withHeader("Authorization", equalTo(String.format("Bearer %s", ACCESS_TOKEN))));
  }

  @Test
  void itRetrievesDiscriminatedUnionList() {
    final List<BrokenDiscriminatedUnion> brokenUnionList = buildBrokenUnionList();
    final String json =
        ResourceReader.readString("data/discriminatedunionnodiscriminator/brokenlist.json");

    final UrlPathPattern urlPathPattern = urlPathEqualTo("/union-serde/get-union-list");
    final MappingBuilder mappingBuilder =
        get(urlPathPattern)
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(json));
    stubFor(mappingBuilder);

    final List<BrokenDiscriminatedUnion.Raw> actualList = unionSerdeApi.getUnionList();
    final List<BrokenDiscriminatedUnion.Raw> expectedList =
        brokenUnionList.stream()
            .map(BrokenDiscriminatedUnion::toBrokenDiscriminatedUnionRaw)
            .collect(Collectors.toList());
    assertThat(actualList).usingRecursiveComparison().isEqualTo(expectedList);
  }

  @Test
  @SneakyThrows
  void itSendsDiscriminatedUnion() {
    final FirstChild firstChild = (FirstChild) buildBrokenUnionList().get(0);

    final UrlPathPattern urlPathPattern = urlPathEqualTo("/union-serde/send-union");
    final MappingBuilder mappingBuilder =
        post(urlPathPattern).willReturn(aResponse().withStatus(204));
    stubFor(mappingBuilder);

    unionSerdeApi.sendUnion(firstChild.toBrokenDiscriminatedUnionRaw());

    final String json =
        ResourceReader.readString("data/discriminatedunionnodiscriminator/firstchild.json");
    final RequestPatternBuilder requestPatternBuilder =
        postRequestedFor(urlPathPattern).withRequestBody(equalToJson(json));
    verify(requestPatternBuilder);
  }

  @Test
  void itSendsDiscriminatedUnionList() {
    final List<BrokenDiscriminatedUnion> brokenUnionList = buildBrokenUnionList();
    final String json =
        ResourceReader.readString("data/discriminatedunionnodiscriminator/brokenlist.json");

    final UrlPathPattern urlPathPattern = urlPathEqualTo("/union-serde/send-union-list");
    final MappingBuilder mappingBuilder =
        post(urlPathPattern).willReturn(aResponse().withStatus(204));
    stubFor(mappingBuilder);

    final List<BrokenDiscriminatedUnion.Raw> requestBody =
        brokenUnionList.stream()
            .map(BrokenDiscriminatedUnion::toBrokenDiscriminatedUnionRaw)
            .collect(Collectors.toList());
    unionSerdeApi.sendUnionList(requestBody);

    final RequestPatternBuilder requestPatternBuilder =
        postRequestedFor(urlPathPattern).withRequestBody(equalToJson(json));
    verify(requestPatternBuilder);
  }
}

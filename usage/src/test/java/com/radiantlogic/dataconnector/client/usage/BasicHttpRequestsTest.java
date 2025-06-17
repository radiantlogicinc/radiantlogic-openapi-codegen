package com.radiantlogic.dataconnector.client.usage;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.radiantlogic.dataconnector.client.usage.ApiClientSupport.ACCESS_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.radiantlogic.custom.dataconnector.radiantonev8api.api.DirectoryNamespaceApiApi;
import com.radiantlogic.custom.dataconnector.radiantonev8api.invoker.ApiClient;
import com.radiantlogic.custom.dataconnector.radiantonev8api.model.CacheProperties;
import com.radiantlogic.custom.dataconnector.radiantonev8api.model.NamingContextNode;
import com.radiantlogic.custom.dataconnector.radiantonev8api.model.NamingContextNodeList;
import com.radiantlogic.custom.dataconnector.radiantonev8api.model.NewNamingContextResponse;
import com.radiantlogic.custom.dataconnector.radiantonev8api.model.NewRootNamingContextDn;
import com.radiantlogic.custom.dataconnector.radiantonev8api.model.NodeType;
import com.radiantlogic.custom.dataconnector.radiantonev8api.model.Pagination;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@WireMockTest(httpPort = 9000)
public class BasicHttpRequestsTest {
  private static ApiClient apiClient;
  private DirectoryNamespaceApiApi directoryNamespaceApi;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeAll
  static void beforeAll() {
    apiClient = ApiClientSupport.createAndAuthenticateRadiantoneApi();
  }

  @BeforeEach
  void setUp() {
    directoryNamespaceApi = new DirectoryNamespaceApiApi(apiClient);
  }

  @Test
  void testGetRootNamingContexts() throws Exception {
    // Prepare test data
    final NamingContextNodeList expectedResponse = new NamingContextNodeList();
    final List<NamingContextNode> nodes = new ArrayList<>();

    final NamingContextNode node1 = new NamingContextNode();
    node1.setDn("o=example");
    node1.setNodeType(NodeType.CONTAINER);
    node1.setIsActive(true);
    nodes.add(node1);

    final NamingContextNode node2 = new NamingContextNode();
    node2.setDn("o=test");
    node2.setNodeType(NodeType.CONTAINER);
    node2.setIsActive(false);
    nodes.add(node2);

    expectedResponse.setNodes(nodes);

    final Pagination pagination = new Pagination();
    pagination.setOffset(0L);
    pagination.setLimit(10L);
    expectedResponse.setPagination(pagination);

    // Set up WireMock stubbing
    stubFor(
        get(urlPathEqualTo("/directory-namespace-service/naming_contexts"))
            .withQueryParam("activeOnly", equalTo("false"))
            .withHeader("Authorization", equalTo("Bearer " + ACCESS_TOKEN))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(expectedResponse))));

    // Execute the API call
    final NamingContextNodeList result =
        directoryNamespaceApi.getRootNamingContexts(false, null, null, null, null, null);

    // Verify the response with assertj
    assertThat(result).isNotNull();
    assertThat(result.getNodes()).hasSize(2);
    assertThat(result.getNodes().get(0).getDn()).isEqualTo("o=example");
    assertThat(result.getNodes().get(0).getIsActive()).isTrue();
    assertThat(result.getNodes().get(1).getDn()).isEqualTo("o=test");
    assertThat(result.getNodes().get(1).getIsActive()).isFalse();
  }

  @Test
  void testAddNewRootNamingContext() throws Exception {
    // Prepare test data
    final NewRootNamingContextDn request = new NewRootNamingContextDn();
    request.setDn("o=newcontext");

    final NewNamingContextResponse expectedResponse = new NewNamingContextResponse();
    expectedResponse.setDn("o=newcontext");

    // Set up WireMock stubbing
    stubFor(
        post(urlPathEqualTo("/directory-namespace-service/naming_contexts"))
            .withRequestBody(equalToJson(objectMapper.writeValueAsString(request)))
            .withHeader("Authorization", equalTo("Bearer " + ACCESS_TOKEN))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(expectedResponse))));

    // Execute the API call
    final NewNamingContextResponse result = directoryNamespaceApi.addNewRootNamingContext(request);

    // Verify the response with assertj
    assertThat(result).isNotNull();
    assertThat(result.getDn()).isEqualTo("o=newcontext");
  }

  @Test
  void testUpdateCacheProperties() throws Exception {
    // Prepare test data
    final String dn = "o=example";
    final CacheProperties cacheProperties = new CacheProperties();
    cacheProperties.setIsActive(true);
    cacheProperties.setIsFullTextSearch(true);
    cacheProperties.setStorageLocation("/tmp/cache");

    // Set up WireMock stubbing
    stubFor(
        put(urlEqualTo("/directory-namespace-service/caches/o%3Dexample/properties"))
            .withRequestBody(equalToJson(objectMapper.writeValueAsString(cacheProperties)))
            .withHeader("Authorization", equalTo("Bearer " + ACCESS_TOKEN))
            .willReturn(aResponse().withStatus(204)));

    // Execute the API call
    directoryNamespaceApi.updateCacheProperties(dn, cacheProperties);

    // Verify that the request was made as expected
    verify(
        putRequestedFor(urlEqualTo("/directory-namespace-service/caches/o%3Dexample/properties"))
            .withRequestBody(equalToJson(objectMapper.writeValueAsString(cacheProperties)))
            .withHeader("Authorization", equalTo("Bearer " + ACCESS_TOKEN)));
  }

  @Test
  void testDeleteNamingContextNode() throws Exception {
    // Prepare test data
    final String dn = "o=example";

    // Set up WireMock stubbing
    stubFor(
        delete(urlEqualTo("/directory-namespace-service/naming_contexts/o%3Dexample"))
            .withHeader("Authorization", equalTo("Bearer " + ACCESS_TOKEN))
            .willReturn(aResponse().withStatus(204)));

    // Execute the API call
    directoryNamespaceApi.deleteNamingContextNode(dn);

    // Verify that the request was made as expected
    verify(
        deleteRequestedFor(urlEqualTo("/directory-namespace-service/naming_contexts/o%3Dexample"))
            .withHeader("Authorization", equalTo("Bearer " + ACCESS_TOKEN)));
  }
}

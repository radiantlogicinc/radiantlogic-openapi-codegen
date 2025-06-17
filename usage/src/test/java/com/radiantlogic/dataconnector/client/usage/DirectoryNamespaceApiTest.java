package com.radiantlogic.dataconnector.client.usage;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DirectoryNamespaceApiTest extends BasicHttpRequestsTest {

  private DirectoryNamespaceApiApi directoryNamespaceApi;
  private ObjectMapper objectMapper;
  private ApiClient apiClient;

  @BeforeEach
  void setUp() {
    apiClient = ApiClientSupport.createAndAuthenticateRadiantoneApi();
    directoryNamespaceApi = new DirectoryNamespaceApiApi(apiClient);
    objectMapper = new ObjectMapper();
  }

  @Test
  void testGetRootNamingContexts() throws Exception {
    // Prepare test data
    NamingContextNodeList expectedResponse = new NamingContextNodeList();
    List<NamingContextNode> nodes = new ArrayList<>();

    NamingContextNode node1 = new NamingContextNode();
    node1.setDn("o=example");
    node1.setNodeType(NodeType.CONTAINER);
    node1.setIsActive(true);
    nodes.add(node1);

    NamingContextNode node2 = new NamingContextNode();
    node2.setDn("o=test");
    node2.setNodeType(NodeType.CONTAINER);
    node2.setIsActive(false);
    nodes.add(node2);

    expectedResponse.setNodes(nodes);

    Pagination pagination = new Pagination();
    pagination.setOffset(0L);
    pagination.setLimit(10L);
    expectedResponse.setPagination(pagination);

    // Set up WireMock stubbing
    stubFor(
        get(urlPathEqualTo("/directory-namespace-service/naming_contexts"))
            .withQueryParam("activeOnly", equalTo("false"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(expectedResponse))));

    // Execute the API call
    NamingContextNodeList result =
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
    NewRootNamingContextDn request = new NewRootNamingContextDn();
    request.setDn("o=newcontext");

    NewNamingContextResponse expectedResponse = new NewNamingContextResponse();
    expectedResponse.setDn("o=newcontext");

    // Set up WireMock stubbing
    stubFor(
        post(urlPathEqualTo("/directory-namespace-service/naming_contexts"))
            .withRequestBody(equalToJson(objectMapper.writeValueAsString(request)))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(expectedResponse))));

    // Execute the API call
    NewNamingContextResponse result = directoryNamespaceApi.addNewRootNamingContext(request);

    // Verify the response with assertj
    assertThat(result).isNotNull();
    assertThat(result.getDn()).isEqualTo("o=newcontext");
  }

  @Test
  void testUpdateCacheProperties() throws Exception {
    // Prepare test data
    String dn = "o=example";
    CacheProperties cacheProperties = new CacheProperties();
    cacheProperties.setIsActive(true);
    cacheProperties.setIsFullTextSearch(true);
    cacheProperties.setStorageLocation("/tmp/cache");

    // Set up WireMock stubbing
    stubFor(
        put(urlEqualTo("/directory-namespace-service/caches/o%3Dexample/properties"))
            .withRequestBody(equalToJson(objectMapper.writeValueAsString(cacheProperties)))
            .willReturn(aResponse().withStatus(204)));

    // Execute the API call
    directoryNamespaceApi.updateCacheProperties(dn, cacheProperties);

    // Verify that the request was made as expected
    verify(
        putRequestedFor(urlEqualTo("/directory-namespace-service/caches/o%3Dexample/properties"))
            .withRequestBody(equalToJson(objectMapper.writeValueAsString(cacheProperties))));
  }

  @Test
  void testDeleteNamingContextNode() throws Exception {
    // Prepare test data
    String dn = "o=example";

    // Set up WireMock stubbing
    stubFor(
        delete(urlEqualTo("/directory-namespace-service/naming_contexts/o%3Dexample"))
            .willReturn(aResponse().withStatus(204)));

    // Execute the API call
    directoryNamespaceApi.deleteNamingContextNode(dn);

    // Verify that the request was made as expected
    verify(
        deleteRequestedFor(urlEqualTo("/directory-namespace-service/naming_contexts/o%3Dexample")));
  }
}

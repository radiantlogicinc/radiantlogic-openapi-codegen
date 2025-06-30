package com.radiantlogic.openapi.codegen.javaclient.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.radiantlogic.openapi.codegen.javaclient.args.Args;
import com.radiantlogic.openapi.codegen.javaclient.args.ArgsParser;
import com.radiantlogic.openapi.codegen.javaclient.args.ProgramArgStatus;
import com.radiantlogic.openapi.codegen.javaclient.generate.OpenapiParser;
import io.swagger.parser.OpenAPIParser;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

/**
 * This test validates that an OpenAPI spec URL that is provided as an argument will be properly
 * resolved and downloaded.
 */
@WireMockTest(httpPort = 9000)
public class DownloadSpecIT {
  @Test
  @SneakyThrows
  void itDownloadsSpecUrl() {
    final InputStream stream =
        getClass()
            .getClassLoader()
            .getResourceAsStream("openapi/broken-discriminator-test-1.0.0.yaml");
    final String openapiYaml = IOUtils.toString(stream, StandardCharsets.UTF_8);

    final MappingBuilder mappingBuilder =
        get(urlPathEqualTo("/openapi.yaml"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withBody(openapiYaml)
                    .withHeader("Content-Type", "application/yaml"));
    stubFor(mappingBuilder);
    final String url = "http://localhost:9000/openapi.yaml";

    final Args args =
        new Args(ProgramArgStatus.PROCEED, new URI(url).toURL(), ArgsParser.DEFAULT_GROUP_ID);
    final OpenAPIParser openAPIParser = new OpenAPIParser();
    final Path tempFile = Files.createTempFile("openapi", ".yaml");
    final OpenapiParser.TempFileCreator tempFileCreator = () -> tempFile;
    final OpenapiParser parser = new OpenapiParser(args, openAPIParser, tempFileCreator);

    assertThatCode(parser::parse).doesNotThrowAnyException();

    final String actualYaml = Files.readString(tempFile);
    assertThat(actualYaml).isEqualTo(openapiYaml);
  }
}

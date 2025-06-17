package com.radiantlogic.dataconnector.client.usage;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.radiantlogic.custom.dataconnector.openaiapi.model.CreateCompletionRequest;
import java.math.BigDecimal;
import java.util.Arrays;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

/**
 * Some of the openapi types are unions that cannot be represented by the java type system. These
 * types are simply marked as "object". The tests here validate the behavior around this.
 */
public class NoTypeSerdeTest {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @SneakyThrows
  void openaiCreateCompletionRequest() {
    final CreateCompletionRequest request = new CreateCompletionRequest();
    request.setBestOf(1);
    request.setEcho(true);
    request.setPresencePenalty(new BigDecimal("10"));
    request.setModel("o3");
    request.setPrompt(Arrays.asList("hello", "world"));
    request.setStop(Arrays.asList(1, 2, 3));

    final String expectedJson =
        ResourceReader.read("data/notypeserde/create-completion-request.json");
    final String actualJson = objectMapper.writeValueAsString(request);
    assertThatJson(actualJson).isEqualTo(expectedJson);

    final CreateCompletionRequest actualRequest =
        objectMapper.readValue(actualJson, CreateCompletionRequest.class);
    assertThat(actualRequest).usingRecursiveComparison().isEqualTo(request);
  }
}

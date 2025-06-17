package com.radiantlogic.dataconnector.client.usage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.radiantlogic.custom.dataconnector.openaiapi.model.CreateCompletionRequest;
import java.math.BigDecimal;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/**
 * Some of the openapi types are unions that cannot be represented by the java type system. These
 * types are simply marked as "object". The tests here validate the behavior around this.
 */
public class NoTypeSerdeTest {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void openaiCreateCompletionRequest() {
    final CreateCompletionRequest request = new CreateCompletionRequest();
    request.setBestOf(1);
    request.setEcho(true);
    request.setPresencePenalty(new BigDecimal("10"));
    request.setModel("o3");
    request.setPrompt(Arrays.asList("hello", "world"));
    request.setStop(Arrays.asList(1, 2, 3));

    final String json = ResourceReader.read("data/notypeserde/create-completion-request.json");
    throw new RuntimeException();
  }
}

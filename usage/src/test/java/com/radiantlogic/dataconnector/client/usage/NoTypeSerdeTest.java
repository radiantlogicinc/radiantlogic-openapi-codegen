package com.radiantlogic.dataconnector.client.usage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

/**
 * Some of the openapi types are unions that cannot be represented by the java type system. These
 * types are simply marked as "object". The tests here validate the behavior around this.
 */
public class NoTypeSerdeTest {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void openaiCreateCompletionRequest() {
    throw new RuntimeException();
  }
}

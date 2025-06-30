package com.radiantlogic.openapi.usage.javaclient;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.radiantlogic.openapi.generated.snykapi.model.IsPrivate;
import java.math.BigDecimal;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

/**
 * A test class that validates that an object with properties that are renamed between the JSON and
 * POJO will serialize/deserialize.
 */
public class RenamedPropertySerdeTest {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @SneakyThrows
  void itSerializesAndDeserializesIsPrivate() {
    final IsPrivate isPrivate = new IsPrivate();
    isPrivate.setTrue(new BigDecimal(42));
    isPrivate.setFalse(new BigDecimal(24));

    final String expectedJson =
        ResourceReader.readString("data/renamedpropertyserde/is-private.json");

    final String actualJson = objectMapper.writeValueAsString(isPrivate);

    assertThatJson(actualJson).isEqualTo(expectedJson);

    final IsPrivate deserializedIsPrivate = objectMapper.readValue(expectedJson, IsPrivate.class);

    assertThat(deserializedIsPrivate).usingRecursiveComparison().isEqualTo(isPrivate);
  }
}

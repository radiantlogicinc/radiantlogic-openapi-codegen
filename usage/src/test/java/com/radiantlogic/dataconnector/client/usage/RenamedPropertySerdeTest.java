package com.radiantlogic.dataconnector.client.usage;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.radiantlogic.custom.dataconnector.snykapi.model.IsPrivate;
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
    // Create the IsPrivate object
    final IsPrivate isPrivate = new IsPrivate();
    isPrivate.setTrue(new BigDecimal(42));
    isPrivate.setFalse(new BigDecimal(24));

    // Load the expected JSON from the filesystem
    final String expectedJson =
        ResourceReader.readString("data/renamedpropertyserde/is-private.json");

    // Serialize the object to JSON
    final String actualJson = objectMapper.writeValueAsString(isPrivate);

    // Compare the serialized JSON with the expected JSON
    assertThatJson(actualJson).isEqualTo(expectedJson);

    // Deserialize the JSON back to an object
    final IsPrivate deserializedIsPrivate = objectMapper.readValue(expectedJson, IsPrivate.class);

    // Compare the deserialized object with the original object
    assertThat(deserializedIsPrivate).usingRecursiveComparison().isEqualTo(isPrivate);
  }
}

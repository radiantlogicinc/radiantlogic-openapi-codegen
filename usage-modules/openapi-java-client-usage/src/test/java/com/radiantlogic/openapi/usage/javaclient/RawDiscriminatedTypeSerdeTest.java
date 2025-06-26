package com.radiantlogic.openapi.usage.javaclient;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.radiantlogic.custom.dataconnector.openaiapi.model.InputMessageResource;
import com.radiantlogic.custom.dataconnector.openaiapi.model.Item;
import java.util.Map;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

/**
 * This is a test of the de-serialization of the .Raw type for a discriminated union that lacks a
 * discriminator.
 */
public class RawDiscriminatedTypeSerdeTest {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @SneakyThrows
  void itDeserializedRawDiscriminatedType() {
    final InputMessageResource messageItem = new InputMessageResource();
    final String json =
        ResourceReader.readString("data/rawdiscriminatedtypeserde/inputmessageresource.json");
    final Map<String, Object> map =
        objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});

    final Item.Raw expectedRaw = new Item.Raw();
    expectedRaw.putAll(map);

    final Item.Raw raw = objectMapper.readValue(json, Item.Raw.class);
    assertThat(raw).isEqualTo(expectedRaw);
  }

  @Test
  void itSerializesRawDiscriminatedType() {}
}

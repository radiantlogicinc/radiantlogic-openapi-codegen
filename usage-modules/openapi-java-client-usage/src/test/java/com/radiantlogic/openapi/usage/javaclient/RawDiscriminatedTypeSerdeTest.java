package com.radiantlogic.openapi.usage.javaclient;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.radiantlogic.custom.dataconnector.openaiapi.model.InputMessageResource;
import com.radiantlogic.custom.dataconnector.openaiapi.model.RoleEnum;
import com.radiantlogic.custom.dataconnector.openaiapi.model.StatusEnum;
import com.radiantlogic.custom.dataconnector.openaiapi.model.TypeEnum;
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
    messageItem.setType(TypeEnum.MESSAGE);
    messageItem.setRole(RoleEnum.USER);
    messageItem.setId("item_1");
    messageItem.setStatus(StatusEnum.COMPLETED);

    final String json = objectMapper.writeValueAsString(messageItem);
    final String expectedJson =
        ResourceReader.readString("data/rawdiscriminatedtypeserde/inputmessageresource.json");
    assertThatJson(json).isEqualTo(expectedJson);

    // TODO ItemResource and Item need to be tested
  }
}

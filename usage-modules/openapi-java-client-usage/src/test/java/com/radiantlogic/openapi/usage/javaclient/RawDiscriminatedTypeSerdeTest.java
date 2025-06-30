package com.radiantlogic.openapi.usage.javaclient;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.radiantlogic.custom.dataconnector.openaiapi.model.InputFileContent;
import com.radiantlogic.custom.dataconnector.openaiapi.model.InputMessageResource;
import com.radiantlogic.custom.dataconnector.openaiapi.model.Item;
import com.radiantlogic.custom.dataconnector.openaiapi.model.RoleEnum;
import com.radiantlogic.custom.dataconnector.openaiapi.model.StatusEnum;
import com.radiantlogic.custom.dataconnector.openaiapi.model.TypeEnum;
import java.util.Collections;
import java.util.Map;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * This is a test of the de-serialization of the .Raw type for a discriminated union that lacks a
 * discriminator.
 */
public class RawDiscriminatedTypeSerdeTest {
  private final ObjectMapper objectMapper = new ObjectMapper();

  /** InputMessage has a oneOf mapping, a discriminator, but no discriminator mapping */
  @Nested
  class InputMessage {
    @Test
    @SneakyThrows
    void itDeserializedRawDiscriminatedType() {
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
    @SneakyThrows
    void itSerializesRawDiscriminatedType() {
      final InputMessageResource messageItem = new InputMessageResource();
      messageItem.setType(TypeEnum.MESSAGE);
      messageItem.setId("item_1");
      messageItem.setStatus(StatusEnum.COMPLETED);
      messageItem.setContent(Collections.emptyList());
      messageItem.setRole(RoleEnum.USER);

      final Item.Raw raw = messageItem.toItemRaw();
      assertThat(raw.get("type")).isEqualTo("message");

      final String json = objectMapper.writeValueAsString(raw);
      final String expectedJson =
          ResourceReader.readString("data/rawdiscriminatedtypeserde/inputmessageresource.json");
      assertThatJson(json).isEqualTo(expectedJson);
    }

    @Test
    @SneakyThrows
    void itCanGetRawDiscriminatorAnyType() {
      final String json =
          ResourceReader.readString("data/rawdiscriminatedtypeserde/inputmessageresource.json");
      final Item.Raw raw = objectMapper.readValue(json, Item.Raw.class);
      assertThat(raw.getType()).isEqualTo(TypeEnum.MESSAGE);
    }

    @Test
    @SneakyThrows
    void itCanConvertRawToImpl() {
      final InputMessageResource messageItem = new InputMessageResource();
      messageItem.setType(TypeEnum.MESSAGE);
      messageItem.setId("item_1");
      messageItem.setStatus(StatusEnum.COMPLETED);

      final InputFileContent inputFileContent = new InputFileContent();
      inputFileContent.setType(TypeEnum.INPUT_FILE);
      inputFileContent.setFilename("test.txt");

      messageItem.setContent(Collections.singletonList(inputFileContent));
      messageItem.setRole(RoleEnum.USER);

      final String json =
          ResourceReader.readString("data/rawdiscriminatedtypeserde/inputmessageresource.json");
      final Item.Raw raw = objectMapper.readValue(json, Item.Raw.class);
      assertThat(raw.toImplementation(InputMessageResource.class)).isEqualTo(messageItem);
    }

    @Test
    void itCanGetRawDiscriminatorNonEnumType() {
      // TODO what about String? BigDecimal? Int? Double?
      throw new RuntimeException();
    }
  }

  /** InputContent has a oneOf mapping and nothing else. */
  @Nested
  class InputContent {
    @Test
    @SneakyThrows
    void itDeserializedRawDiscriminatedType() {
      throw new RuntimeException();
    }

    @Test
    @SneakyThrows
    void itSerializesRawDiscriminatedType() {
      throw new RuntimeException();
    }

    @Test
    @SneakyThrows
    void itCanGetRawDiscriminatorAnyType() {
      throw new RuntimeException();
    }

    @Test
    @SneakyThrows
    void itCanConvertRawToImpl() {
      throw new RuntimeException();
    }
  }
}

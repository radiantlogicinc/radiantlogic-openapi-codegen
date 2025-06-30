package com.radiantlogic.openapi.usage.javaclient;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.radiantlogic.openapi.generated.openaiapi.model.InputContent;
import com.radiantlogic.openapi.generated.openaiapi.model.InputFileContent;
import com.radiantlogic.openapi.generated.openaiapi.model.InputMessageResource;
import com.radiantlogic.openapi.generated.openaiapi.model.Item;
import com.radiantlogic.openapi.generated.openaiapi.model.RoleEnum;
import com.radiantlogic.openapi.generated.openaiapi.model.StatusEnum;
import com.radiantlogic.openapi.generated.openaiapi.model.TypeEnum;
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
  class InputMessageTests {
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

      final InputFileContent inputFileContent = new InputFileContent();
      inputFileContent.setType(TypeEnum.INPUT_FILE);
      inputFileContent.setFilename("test.txt");

      messageItem.setContent(Collections.singletonList(inputFileContent.toInputContentRaw()));
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

      messageItem.setContent(Collections.singletonList(inputFileContent.toInputContentRaw()));
      messageItem.setRole(RoleEnum.USER);

      final String json =
          ResourceReader.readString("data/rawdiscriminatedtypeserde/inputmessageresource.json");
      final Item.Raw raw = objectMapper.readValue(json, Item.Raw.class);

      final InputMessageResource actualImpl = raw.toImplementation(InputMessageResource.class);
      assertThat(actualImpl).usingRecursiveComparison().isEqualTo(messageItem);

      final InputFileContent actualContentImpl =
          actualImpl.getContent().get(0).toImplementation(InputFileContent.class);
      assertThat(actualContentImpl).usingRecursiveComparison().isEqualTo(inputFileContent);
    }
  }

  /** InputContent has a oneOf mapping and nothing else. */
  @Nested
  class InputContentTests {
    @Test
    @SneakyThrows
    void itDeserializedRawDiscriminatedType() {
      final String json =
          ResourceReader.readString("data/rawdiscriminatedtypeserde/fileinputcontent.json");
      final Map<String, Object> map =
          objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});

      final InputContent.Raw expectedRaw = new InputContent.Raw();
      expectedRaw.putAll(map);

      final InputContent.Raw raw = objectMapper.readValue(json, InputContent.Raw.class);
      assertThat(raw).isEqualTo(expectedRaw);
    }

    @Test
    @SneakyThrows
    void itSerializesRawDiscriminatedType() {
      final InputFileContent inputFileContent = new InputFileContent();
      inputFileContent.setType(TypeEnum.INPUT_FILE);
      inputFileContent.setFilename("test.txt");
      inputFileContent.setFileData("The data");

      final InputContent.Raw raw = inputFileContent.toInputContentRaw();
      assertThat(raw.get("type")).isEqualTo("input_file");

      final String json = objectMapper.writeValueAsString(raw);
      final String expectedJson =
          ResourceReader.readString("data/rawdiscriminatedtypeserde/fileinputcontent.json");
      assertThatJson(json).isEqualTo(expectedJson);
    }

    @Test
    @SneakyThrows
    void itCanConvertRawToImpl() {
      final InputFileContent inputFileContent = new InputFileContent();
      inputFileContent.setType(TypeEnum.INPUT_FILE);
      inputFileContent.setFilename("test.txt");
      inputFileContent.setFileData("The data");

      final String json =
          ResourceReader.readString("data/rawdiscriminatedtypeserde/fileinputcontent.json");
      final InputContent.Raw raw = objectMapper.readValue(json, InputContent.Raw.class);

      final InputFileContent actualImpl = raw.toImplementation(InputFileContent.class);
      assertThat(actualImpl).usingRecursiveComparison().isEqualTo(inputFileContent);
    }
  }

  /**
   * These tests validate the behavior of non-string, non-enum discriminators with some of the Raw
   * type logic.
   */
  @Nested
  class OtherTypeDiscriminators {
    @Test
    void itHandlesNumberDiscriminator() {
      throw new RuntimeException();
    }

    @Test
    void itHandlesBooleanDiscriminator() {
      throw new RuntimeException();
    }

    @Test
    void itHandlesCastDiscriminator() {
      throw new RuntimeException();
    }
  }
}

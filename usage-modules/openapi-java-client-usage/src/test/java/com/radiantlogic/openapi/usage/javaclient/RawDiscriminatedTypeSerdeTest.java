package com.radiantlogic.openapi.usage.javaclient;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.radiantlogic.custom.dataconnector.openaiapi.model.InputMessageResource;
import com.radiantlogic.custom.dataconnector.openaiapi.model.Item;
import com.radiantlogic.custom.dataconnector.openaiapi.model.RoleEnum;
import com.radiantlogic.custom.dataconnector.openaiapi.model.StatusEnum;
import com.radiantlogic.custom.dataconnector.openaiapi.model.TypeEnum;
import java.util.Collections;
import java.util.Map;
import java.util.stream.IntStream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * This is a test of the de-serialization of the .Raw type for a discriminated union that lacks a
 * discriminator.
 */
public class RawDiscriminatedTypeSerdeTest {
  private final ObjectMapper objectMapper = new ObjectMapper();

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

    final String json = objectMapper.writeValueAsString(messageItem);
    final String expectedJson =
        ResourceReader.readString("data/rawdiscriminatedtypeserde/inputmessageresource.json");
    assertThatJson(json).isEqualTo(expectedJson);
  }

  // TODO delete this
  @Test
  void foo() {
    final RestTemplate restTemplate = new RestTemplate();
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
    converter.setObjectMapper(objectMapper);
    final int convertersCount = restTemplate.getMessageConverters().size();
    final int matchingIndex =
        IntStream.range(0, restTemplate.getMessageConverters().size())
            .filter(
                index ->
                    restTemplate.getMessageConverters().get(index)
                        instanceof MappingJackson2HttpMessageConverter)
            .findFirst()
            .orElseThrow(RuntimeException::new);
    restTemplate.getMessageConverters().set(matchingIndex, converter);
    throw new RuntimeException();
  }
}

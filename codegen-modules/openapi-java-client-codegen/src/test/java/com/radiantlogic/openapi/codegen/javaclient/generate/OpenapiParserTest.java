package com.radiantlogic.openapi.codegen.javaclient.generate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

import com.radiantlogic.openapi.codegen.javaclient.args.Args;
import com.radiantlogic.openapi.codegen.javaclient.args.ProgramArgStatus;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.net.URI;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OpenapiParserTest {
  @Mock private OpenAPIParser internalParser;

  @Test
  @SneakyThrows
  void itParsesOpenapi() {
    final Args args = new Args(ProgramArgStatus.PROCEED, new URI("file:/foo/bar.yaml").toURL(), "");
    final OpenapiParser parser = new OpenapiParser(args, internalParser);

    final ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
    final ArgumentCaptor<ParseOptions> parseOptionsArgumentCaptor =
        ArgumentCaptor.forClass(ParseOptions.class);

    final OpenAPI openAPI = new OpenAPI();
    final Info info = new Info();
    info.setTitle("My Title");
    openAPI.setInfo(info);

    final SwaggerParseResult result = new SwaggerParseResult();
    result.setOpenAPI(openAPI);

    when(internalParser.readLocation(
            pathCaptor.capture(), isA(List.class), parseOptionsArgumentCaptor.capture()))
        .thenReturn(result);

    final OpenAPI actual = parser.parse();
    assertThat(actual).isEqualTo(openAPI);

    assertThat(pathCaptor.getValue()).isEqualTo(args.openapiUrl());

    final ParseOptions parseOptions = new ParseOptions();
    parseOptions.setResolve(true);
    parseOptions.setResolveFully(false);
    assertThat(parseOptionsArgumentCaptor.getValue())
        .usingRecursiveComparison()
        .isEqualTo(parseOptions);
  }
}

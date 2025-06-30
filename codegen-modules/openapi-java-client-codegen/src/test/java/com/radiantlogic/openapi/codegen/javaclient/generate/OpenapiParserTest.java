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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OpenapiParserTest {
  @Mock private OpenAPIParser internalParser;

  @TempDir private Path tempDir;

  @Test
  @SneakyThrows
  void itParsesOpenapi() {
    final Path tempFile = tempDir.resolve("openapi.yaml");
    Files.createFile(tempFile);
    final Args args = new Args(ProgramArgStatus.PROCEED, tempFile.toUri().toURL(), "");
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
            isA(String.class), isA(List.class), parseOptionsArgumentCaptor.capture()))
        .thenReturn(result);

    final OpenAPI actual = parser.parse();
    assertThat(actual).isEqualTo(openAPI);

    final ParseOptions parseOptions = new ParseOptions();
    parseOptions.setResolve(true);
    parseOptions.setResolveFully(false);
    assertThat(parseOptionsArgumentCaptor.getValue())
        .usingRecursiveComparison()
        .isEqualTo(parseOptions);
  }
}

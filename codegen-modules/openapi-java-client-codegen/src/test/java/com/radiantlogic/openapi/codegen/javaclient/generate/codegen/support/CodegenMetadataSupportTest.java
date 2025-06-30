package com.radiantlogic.openapi.codegen.javaclient.generate.codegen.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.radiantlogic.openapi.codegen.javaclient.args.Args;
import com.radiantlogic.openapi.codegen.javaclient.args.ProgramArgStatus;
import com.radiantlogic.openapi.codegen.javaclient.io.CodegenPaths;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import java.net.URI;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CodegenMetadataSupportTest {
  private final CodegenMetadataSupport codegenMetadataSupport = new CodegenMetadataSupport();
  private Args args;

  @BeforeEach
  @SneakyThrows
  void beforeEach() {
    args =
        new Args(ProgramArgStatus.PROCEED, new URI("file:/foo/bar.yaml").toURL(), "com.something");
  }

  @Test
  void itGetsSimpleMetadata() {
    final OpenAPI openAPI = createOpenAPI();
    openAPI.getInfo().setTitle("MyTitle");
    openAPI.getInfo().setVersion("1.0.0");
    final var metadata = codegenMetadataSupport.getMetadata(openAPI, args);
    final var expectedMetadata =
        new CodegenMetadataSupport.CodegenMetadata(
            CodegenPaths.OUTPUT_DIR.resolve("MyTitle").resolve("1.0.0"),
            "MyTitle",
            "1.0.0",
            "com.something.mytitle");
    assertThat(metadata).isEqualTo(expectedMetadata);
  }

  @Test
  void itGetsMetadataWithComplexTitle() {
    final OpenAPI openAPI = createOpenAPI();
    openAPI.getInfo().setTitle("My Title - Hello");
    openAPI.getInfo().setVersion("1.0.0");
    final var metadata = codegenMetadataSupport.getMetadata(openAPI, args);
    final var expectedMetadata =
        new CodegenMetadataSupport.CodegenMetadata(
            CodegenPaths.OUTPUT_DIR.resolve("My-Title---Hello").resolve("1.0.0"),
            "My-Title---Hello",
            "1.0.0",
            "com.something.mytitlehello");
    assertThat(metadata).isEqualTo(expectedMetadata);
  }

  @Test
  void itGetsMetadataWithoutInfo() {
    final OpenAPI openAPI = createOpenAPI();
    openAPI.setInfo(null);
    final var metadata = codegenMetadataSupport.getMetadata(openAPI, args);
    final var expectedMetadata =
        new CodegenMetadataSupport.CodegenMetadata(
            CodegenPaths.OUTPUT_DIR.resolve("unknown-api").resolve("unknown-version"),
            "unknown-api",
            "unknown-version",
            "com.something.unknownapi");
    assertThat(metadata).isEqualTo(expectedMetadata);
  }

  @Test
  void itGetsMetadataWithTitleWithLeadingNumbers() {
    final OpenAPI openAPI = createOpenAPI();
    openAPI.getInfo().setTitle("1MyTitle");
    openAPI.getInfo().setVersion("1.0.0");
    final var metadata = codegenMetadataSupport.getMetadata(openAPI, args);
    final var expectedMetadata =
        new CodegenMetadataSupport.CodegenMetadata(
            CodegenPaths.OUTPUT_DIR.resolve("1MyTitle").resolve("1.0.0"),
            "1MyTitle",
            "1.0.0",
            "com.something.onemytitle");
    assertThat(metadata).isEqualTo(expectedMetadata);
  }

  private OpenAPI createOpenAPI() {
    final OpenAPI openAPI = new OpenAPI();
    openAPI.setInfo(new Info());
    return openAPI;
  }
}

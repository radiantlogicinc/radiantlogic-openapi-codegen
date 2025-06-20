package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.radiantlogic.dataconnector.restapi.javaclient.builder.args.Args;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.args.ProgramArgStatus;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.io.CodegenPaths;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.junit.jupiter.api.Test;

public class CodegenMetadataSupportTest {
  private final CodegenMetadataSupport codegenMetadataSupport = new CodegenMetadataSupport();
  private final Args args = new Args(ProgramArgStatus.PROCEED, "/foo/bar.yaml", "com.something");

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
    throw new RuntimeException();
  }

  @Test
  void itGetsMetadataWithoutInfo() {
    throw new RuntimeException();
  }

  @Test
  void itGetsMetadataWithTitleWithLeadingNumbers() {
    throw new RuntimeException();
  }

  private OpenAPI createOpenAPI() {
    final OpenAPI openAPI = new OpenAPI();
    openAPI.setInfo(new Info());
    return openAPI;
  }
}

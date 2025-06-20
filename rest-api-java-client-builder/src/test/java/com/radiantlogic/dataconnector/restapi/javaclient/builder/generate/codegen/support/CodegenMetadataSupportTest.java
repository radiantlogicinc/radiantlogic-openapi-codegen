package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support;

import com.radiantlogic.dataconnector.restapi.javaclient.builder.args.Args;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.args.ProgramArgStatus;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.junit.jupiter.api.Test;

public class CodegenMetadataSupportTest {
  private final CodegenMetadataSupport codegenMetadataSupport = new CodegenMetadataSupport();
  private final Args args = new Args(ProgramArgStatus.PROCEED, "/foo/bar.yaml", "com.something");

  @Test
  void itGetsSimpleMetadata() {
    throw new RuntimeException();
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

package com.radiantlogic.dataconnector.codegen.generate;

import com.radiantlogic.dataconnector.codegen.io.CodegenPaths;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import java.nio.file.Path;
import java.util.Optional;
import lombok.NonNull;
import org.openapitools.codegen.languages.JavaClientCodegen;

public class DataconnectorJavaClientCodegen extends JavaClientCodegen {
  public DataconnectorJavaClientCodegen(@NonNull final OpenAPI openAPI) {
    setOpenAPI(openAPI);
    init();
  }

  private void init() {
    final String title = getOpenapiTitle();
    final String version = getOpenapiVersion();
    final Path outputDir = CodegenPaths.OUTPUT_DIR.resolve(title).resolve(version);
    setOutputDir(outputDir.toString());
    setGroupId("com.radiantlogic.customer.dataconnector");
    setArtifactId(title);
    setArtifactVersion(version);
  }

  // TODO need tests for these utilities
  private String getOpenapiTitle() {
    return Optional.ofNullable(openAPI.getInfo())
        .map(Info::getTitle)
        .map(title -> title.replaceAll("\\s+", "-"))
        .orElse("unknown-api");
  }

  private String getOpenapiVersion() {
    return Optional.ofNullable(openAPI.getInfo()).map(Info::getVersion).orElse("unknown-version");
  }
}

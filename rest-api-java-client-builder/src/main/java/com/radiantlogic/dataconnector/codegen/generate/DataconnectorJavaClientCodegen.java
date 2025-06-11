package com.radiantlogic.dataconnector.codegen.generate;

import com.radiantlogic.dataconnector.codegen.args.Args;
import com.radiantlogic.dataconnector.codegen.io.CodegenPaths;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import lombok.NonNull;
import org.apache.commons.io.FileUtils;
import org.openapitools.codegen.languages.JavaClientCodegen;

/**
 * A customized version of the default JavaClientCodegen designed to produce the exact artifact
 * style we want.
 */
public class DataconnectorJavaClientCodegen extends JavaClientCodegen {
  public DataconnectorJavaClientCodegen(@NonNull final OpenAPI openAPI, @NonNull final Args args) {
    setOpenAPI(openAPI);
    init(args);
  }

  private void init(@NonNull final Args args) {
    final String title = getOpenapiTitle();
    final String version = getOpenapiVersion();
    final Path outputDir = CodegenPaths.OUTPUT_DIR.resolve(title).resolve(version);
    if (Files.exists(outputDir)) {
      try {
        FileUtils.deleteDirectory(outputDir.toFile());
      } catch (IOException ex) {
        throw new RuntimeException(
            "Unable to delete existing output directory: %s".formatted(outputDir), ex);
      }
    }
    setOutputDir(outputDir.toString());
    setGroupId(args.groupId());
    // TODO need to validate groupId
    setApiPackage("%s.api".formatted(args.groupId()));
    setModelPackage("%s.model".formatted(args.groupId()));
    setInvokerPackage("%s.invoker".formatted(args.groupId()));
    setArtifactId(title);
    setArtifactVersion(version);
    setDisallowAdditionalPropertiesIfNotPresent(false);
    setLibrary("apache-httpclient");
    setUseOneOfInterfaces(true);

    // TODO need to fix the scm output
    // TODO need to fix the license output
    // TODO need to fix the dev output
    // TODO need to not be fat jar
    // TODO needs to be unsigned
  }

  // TODO need tests
  private String getOpenapiTitle() {
    return Optional.ofNullable(openAPI.getInfo())
        .map(Info::getTitle)
        .map(title -> title.replaceAll("\\s+", "-"))
        .orElse("unknown-api");
  }

  // TODO need tests
  private String getOpenapiVersion() {
    return Optional.ofNullable(openAPI.getInfo()).map(Info::getVersion).orElse("unknown-version");
  }
}

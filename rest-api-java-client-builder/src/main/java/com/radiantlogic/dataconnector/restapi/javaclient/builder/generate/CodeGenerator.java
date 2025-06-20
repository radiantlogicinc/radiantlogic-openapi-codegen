package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.openapitools.codegen.ClientOptInput;
import org.openapitools.codegen.CodegenConfig;
import org.openapitools.codegen.CodegenConstants;
import org.openapitools.codegen.DefaultGenerator;

/** Setup the output directory and generate the call. */
@Slf4j
@RequiredArgsConstructor
public class CodeGenerator {
  @NonNull private final CodegenConfig codegen;

  public void generate() {
    log.info("Generating code");

    log.debug("Performing code generation");
    prepareOutputDirectory(codegen.getOutputDir(), codegen.getIgnorePatterns());

    final DefaultGenerator generator = new DefaultGenerator();
    generator.setGeneratorPropertyDefault(CodegenConstants.SKIP_FORM_MODEL, "false");

    generator.opts(new ClientOptInput().config(codegen).openAPI(openAPI)).generate();
  }

  private void prepareOutputDirectory(
      @NonNull final String outputDir, @NonNull final List<String> ignorePatterns) {
    try {
      final Path path = Path.of(outputDir);
      if (Files.exists(path)) {
        FileUtils.deleteDirectory(path.toFile());
      }
      Files.createDirectories(path);
      writeIgnorePatterns(path, ignorePatterns);
    } catch (final IOException ex) {
      throw new IllegalStateException(
          "Unable to prepare output directory: %s".formatted(outputDir), ex);
    }
  }

  private void writeIgnorePatterns(
      @NonNull final Path outputDir, @NonNull final List<String> ignorePatterns) {
    final Path ignoreFile = outputDir.resolve(".openapi-generator-ignore");
    try {
      Files.write(ignoreFile, ignorePatterns);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}

package com.radiantlogic.openapi.builder.javaclient.generate.codegen.support;

import com.radiantlogic.openapi.builder.javaclient.args.Args;
import com.radiantlogic.openapi.builder.javaclient.io.CodegenPaths;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;

/** Prepare metadata values needed by the codegen */
public class CodegenMetadataSupport {
  public record CodegenMetadata(
      @NonNull Path outputDir,
      @NonNull String artifactId,
      @NonNull String version,
      @NonNull String basePackage) {}

  public CodegenMetadata getMetadata(@NonNull final OpenAPI openAPI, @NonNull final Args args) {
    final String title = getOpenapiTitle(openAPI);
    final String version = getOpenapiVersion(openAPI);

    final Path outputDir = CodegenPaths.OUTPUT_DIR.resolve(title).resolve(version);
    final String basePackage =
        fixLeadingNumbers("%s.%s".formatted(args.groupId(), ensureValidPackageName(title)));
    return new CodegenMetadata(outputDir, title, version, basePackage);
  }

  private static String getOpenapiTitle(@NonNull final OpenAPI openAPI) {
    return Optional.ofNullable(openAPI.getInfo())
        .map(Info::getTitle)
        .map(title -> title.replaceAll("\\s+", "-").replace("&", ""))
        .orElse("unknown-api");
  }

  private String getOpenapiVersion(@NonNull final OpenAPI openAPI) {
    return Optional.ofNullable(openAPI.getInfo()).map(Info::getVersion).orElse("unknown-version");
  }

  /** Remove any characters that would make this a non-compliant java package name. */
  private static String ensureValidPackageName(@NonNull final String packageName) {
    return packageName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
  }

  /**
   * The package name generated here must conform to valid java package name rules. If a package
   * element ends up with a leading number, that cannot be allowed.
   */
  private static String fixLeadingNumbers(@NonNull final String packageName) {
    return Arrays.stream(packageName.split("\\."))
        .map(
            name -> {
              final String beginning =
                  switch (name.charAt(0)) {
                    case '0' -> "zero";
                    case '1' -> "one";
                    case '2' -> "two";
                    case '3' -> "three";
                    case '4' -> "four";
                    case '5' -> "five";
                    case '6' -> "six";
                    case '7' -> "seven";
                    case '8' -> "eight";
                    case '9' -> "nine";
                    default -> "%s".formatted(name.charAt(0));
                  };
              return "%s%s".formatted(beginning, name.substring(1));
            })
        .collect(Collectors.joining("."));
  }
}

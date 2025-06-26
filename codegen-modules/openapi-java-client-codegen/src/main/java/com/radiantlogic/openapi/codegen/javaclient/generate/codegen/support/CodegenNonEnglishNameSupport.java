package com.radiantlogic.openapi.codegen.javaclient.generate.codegen.support;

import io.swagger.v3.oas.models.OpenAPI;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

/**
 * Fixes non-english (primarily chinese) names in the OpenAPI spec to ensure the method names
 * created are valid Java.
 */
public class CodegenNonEnglishNameSupport {
  private static final Pattern NON_ENGLISH_PATTERN = Pattern.compile("[^\\p{ASCII}]");
  private static final Pattern NON_LETTER_PATTERN = Pattern.compile("[\\W0-9]+");

  public void fixOperationIds(@NonNull final OpenAPI openAPI) {
    openAPI.getPaths().entrySet().stream()
        .flatMap(pathEntry -> pathEntry.getValue().readOperations().stream())
        .filter(operation -> Objects.nonNull(operation.getOperationId()))
        .forEach(
            operation -> {
              final Matcher matcher = NON_ENGLISH_PATTERN.matcher(operation.getOperationId());
              final String withoutNonEnglish = matcher.replaceAll("");
              final String withoutNonLetter =
                  NON_LETTER_PATTERN.matcher(withoutNonEnglish).replaceAll("");
              if (StringUtils.isNotBlank(withoutNonLetter)) {
                operation.setOperationId(withoutNonEnglish);
              } else {
                operation.setOperationId(null);
              }
            });
  }
}

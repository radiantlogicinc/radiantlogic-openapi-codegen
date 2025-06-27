package com.radiantlogic.openapi.codegen.javaclient.generate.codegen.support;

import com.radiantlogic.openapi.codegen.javaclient.generate.codegen.utils.CodegenConstants;
import io.swagger.v3.oas.models.OpenAPI;
import java.util.Objects;
import java.util.regex.Matcher;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

/**
 * Fixes non-english (primarily chinese) names in the OpenAPI spec to ensure the method names
 * created are valid Java.
 */
public class CodegenNonEnglishNameSupport {

  public void fixOperationIds(@NonNull final OpenAPI openAPI) {
    openAPI.getPaths().entrySet().stream()
        .flatMap(pathEntry -> pathEntry.getValue().readOperations().stream())
        .filter(operation -> Objects.nonNull(operation.getOperationId()))
        .forEach(
            operation -> {
              final Matcher matcher =
                  CodegenConstants.NON_ENGLISH_PATTERN.matcher(operation.getOperationId());
              final String withoutNonEnglish = matcher.replaceAll("");
              final String withoutNonLetter =
                  CodegenConstants.NON_LETTER_PATTERN.matcher(withoutNonEnglish).replaceAll("");
              if (StringUtils.isNotBlank(withoutNonLetter)) {
                operation.setOperationId(withoutNonEnglish);
              } else {
                operation.setOperationId(null);
              }
            });
  }
}

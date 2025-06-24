package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support;

import lombok.NonNull;
import org.junit.jupiter.api.Test;

public class CodegenFilenameSupportTest {
  @Test
  void itDoesNothingIfNoFilenamesClash() {
    throw new RuntimeException();
  }

  @Test
  void itAddsSuffixToNameAndFixesImportsIfClash() {
    throw new RuntimeException();
  }

  @NonNull
  private String modelFilename(
      @NonNull final String templateName, @NonNull final String modelName) {
    return modelName;
  }
}

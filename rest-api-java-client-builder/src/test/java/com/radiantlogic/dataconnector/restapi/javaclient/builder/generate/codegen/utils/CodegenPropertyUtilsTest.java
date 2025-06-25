package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class CodegenPropertyUtilsTest {
  @Nested
  class IsEnumProperty {
    @Test
    void itIsEnum() {
      throw new RuntimeException();
    }

    @Test
    void itIsEnumRef() {
      throw new RuntimeException();
    }

    @Test
    void itIsInnerEnum() {
      throw new RuntimeException();
    }

    @Test
    void itIsNotEnum() {
      throw new RuntimeException();
    }
  }

  @Nested
  class IsSamePropertyInChild {
    @Test
    void itIsSameBasename() {
      throw new RuntimeException();
    }

    @Test
    void itIsNotSameBasename() {
      throw new RuntimeException();
    }
  }

  @Nested
  class IsEnumRefProp {
    @Test
    void itIsEnumAndNotOthers() {
      throw new RuntimeException();
    }

    @Test
    void itIsInnerEnumAndNotOthers() {
      throw new RuntimeException();
    }

    @Test
    void itIsEnumRefAndNotOthers() {
      throw new RuntimeException();
    }
  }
}

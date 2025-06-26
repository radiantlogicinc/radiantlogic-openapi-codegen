package com.radiantlogic.openapi.codegen.javaclient.io;

import java.nio.file.Path;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CodegenPaths {
  public static final Path OUTPUT_DIR = Path.of(System.getProperty("user.dir"), "output");
}

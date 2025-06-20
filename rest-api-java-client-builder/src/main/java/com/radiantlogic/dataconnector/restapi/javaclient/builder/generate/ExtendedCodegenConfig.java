package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate;

import java.util.Set;
import org.openapitools.codegen.CodegenConfig;

public interface ExtendedCodegenConfig extends CodegenConfig {
  Set<String> getIgnorePatterns();
}

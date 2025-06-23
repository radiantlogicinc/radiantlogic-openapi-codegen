package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.openapitools.codegen.CodegenConfig;
import org.openapitools.codegen.languages.AbstractJavaCodegen;

/**
 * A decorator that wraps around an implementation of a Java codegen. This applies enhancements to
 * how the codegen operates. The benefits to this decorator approach rather than extending is that
 * common changes that could apply to multiple Java codegens (server & client) can be shared easily
 * with this delegate.
 */
@RequiredArgsConstructor
public class JavaCodegenDecorator implements CodegenConfig {
  @Delegate(types = CodegenConfig.class)
  private final AbstractJavaCodegen delegate;
}

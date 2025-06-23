package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen;

import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support.CodegenDiscriminatorTypeSupport;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support.CodegenMetadataSupport;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support.CodegenNonEnglishNameSupport;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support.CodegenUnsupportedUnionTypeSupport;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.openapitools.codegen.CodegenConfig;
import org.openapitools.codegen.languages.AbstractJavaCodegen;

/**
 * A decorator that wraps around an implementation of a Java codegen. This applies enhancements to
 * how the codegen operates. The benefits to this decorator approach rather than extending is that
 * common changes that could apply to multiple Java codegens (server & client) can be shared easily
 * with this decorator.
 */
@RequiredArgsConstructor
public class JavaCodegenDecorator implements CodegenConfig {
  /**
   * This lombok delegate means that any interface method not explicitly implemented in this class
   * will implicitly be delegated to this instance.
   */
  @Delegate(types = CodegenConfig.class)
  @NonNull
  private final AbstractJavaCodegen delegate;

  private final CodegenMetadataSupport codegenMetadataSupport = new CodegenMetadataSupport();
  private final CodegenUnsupportedUnionTypeSupport codegenUnsupportedUnionTypeSupport =
      new CodegenUnsupportedUnionTypeSupport();
  private final CodegenDiscriminatorTypeSupport codegenDiscriminatorTypeSupport =
      new CodegenDiscriminatorTypeSupport();
  private final CodegenNonEnglishNameSupport codegenNonEnglishNameSupport =
      new CodegenNonEnglishNameSupport();
}

package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate;

import org.mapstruct.Mapper;
import org.openapitools.codegen.CodegenProperty;

@Mapper
public interface CodegenMapper {
  ExtendedCodegenProperty extendProperty(final CodegenProperty prop);
}

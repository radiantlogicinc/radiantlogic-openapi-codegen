package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate;

import org.mapstruct.Mapper;
import org.openapitools.codegen.CodegenProperty;

@Mapper
public interface CodegenPropertyMapper {
  ExtendedCodegenProperty extendProperty(final CodegenProperty prop);
}

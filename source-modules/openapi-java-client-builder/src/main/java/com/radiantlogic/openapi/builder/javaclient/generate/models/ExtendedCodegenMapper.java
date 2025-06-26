package com.radiantlogic.openapi.builder.javaclient.generate.models;

import org.mapstruct.Mapper;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenProperty;

/**
 * A MapStruct mapper that copies all properties of the default classes to their extended
 * counterparts.
 */
@Mapper
public interface ExtendedCodegenMapper {
  ExtendedCodegenProperty extendProperty(final CodegenProperty prop);

  ExtendedCodegenModel extendModel(final CodegenModel model);
}

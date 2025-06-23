package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support;

/**
 * The term "flattened" comes from the source code of openapi-generator when the type is configured
 * in this way. In short, a type like this is a union type of non-object types, ie `Long | String |
 * Boolean`. Such a thing is valid in OpenAPI & JSON, but not in Java.
 *
 * <p>This class inspects the properties, identifies incorrectly "flattened" types, and replaces
 * them with `Object` because a more precise union would be extraordinarily difficult to configure
 * the generator to support.
 */
public class CodegenFlattenedComplexTypeSupport {}

package com.radiantlogic.openapi.codegen.javaclient.generate.codegen.support;

/**
 * The "Raw" types are automatically added to discriminated union interfaces if they lack the
 * mapping information to correctly serialize/de-serialize to/from the sub types. This is a
 * workaround to compensate for an incomplete OpenAPI specification.
 *
 * <p>This support class updates models and operations so that any usage of a discriminated union
 * lacking its mapping information is adjusted to use the Raw type instead.
 */
public class CodegenRawTypeUsageSupport {}

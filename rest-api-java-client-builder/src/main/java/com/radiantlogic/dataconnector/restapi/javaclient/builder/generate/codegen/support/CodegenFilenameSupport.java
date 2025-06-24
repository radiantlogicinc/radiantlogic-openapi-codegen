package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support;

/**
 * There is an issue where anonymous schemas will be automatically assigned names by
 * openapi-generator. Those names may clash with names of schemas that were explicitly declared in
 * the spec. The generator doesn't catch these because they still end up differing by case, but many
 * filesystems don't support case-sensitive names and so when things are written out one will
 * overwrite the other and then the output is invalid.
 *
 * <p>This identifies and adds a suffix to names that will clash when being written out, so that
 * they are safe and everything will be written correctly.
 */
public class CodegenFilenameSupport {
  // TODO finish this
}

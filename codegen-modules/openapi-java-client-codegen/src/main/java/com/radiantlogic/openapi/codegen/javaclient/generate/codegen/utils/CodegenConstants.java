package com.radiantlogic.openapi.codegen.javaclient.generate.codegen.utils;

import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CodegenConstants {
  public static final String IMPORTS_KEY = "imports";
  public static final String IMPORT_KEY = "import";
  public static final String IMPORT_PATH_KEY = "importPath";
  public static final String MODEL_TEMPLATE = "model.mustache";
  public static final String ENUM_VARS_KEY = "enumVars";
  public static final String VALUES_KEY = "values";
  public static final String NAME_KEY = "name";
  public static final String VALUE_KEY = "value";
  public static final String IS_STRING_KEY = "isString";
  public static final Pattern LIST_TYPE_PATTERN = Pattern.compile("^List<(.*)>$");
  public static final Pattern QUOTED_STRING_PATTERN = Pattern.compile("^\"(.*)\"$");
  public static final Pattern SCHEMA_REF_PATTERN = Pattern.compile("^#/components/schemas/(.*)$");
  public static final Pattern NON_ENGLISH_PATTERN = Pattern.compile("[^\\p{ASCII}]");
  public static final Pattern NON_LETTER_PATTERN = Pattern.compile("[\\W0-9]+");
}

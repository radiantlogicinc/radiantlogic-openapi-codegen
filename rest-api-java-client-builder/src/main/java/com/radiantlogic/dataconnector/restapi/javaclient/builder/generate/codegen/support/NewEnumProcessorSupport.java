package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support;

/**
 * Other support classes extract a lot of enums from models. These are inline enums that should
 * actually be separate models because inline enums produce more potential compile errors than any
 * other type. This class processes all the new enums, merges duplicates together (preserving all
 * enum values in the process), merges new enums with matching existing ones, etc. In the end it
 * merges the new enums into the existing ModelMaps map so that the codegen can correctly generate
 * the necessary model classes.
 */
public class NewEnumProcessorSupport {}

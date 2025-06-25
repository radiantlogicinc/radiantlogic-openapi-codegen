package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support;

import java.util.Map;
import lombok.NonNull;
import org.openapitools.codegen.model.ModelsMap;

/**
 * Inherited enums are one of the most challenging parts of how the openapi-generator works,
 * especially inline inherited enums which tend to be duplicated at each level of the class
 * hierarchy and therefore cause compiler errors.
 *
 * <p>This class identifies all enums throughout the inheritance hierarchy and ensures they are all
 * references to enums in separate class files. This means no inline enums anywhere in the
 * hierarchy. It also ensures that properties that represent the same enum are unified so that the
 * enum reference contains all necessary values.
 *
 * <p>Lastly, it will return a collection of all the enums that need to be added to the models map
 * to ensure that the files are all created correctly.
 */
public class CodegenInheritedEnumSupport {
  // TODO finish this

  public void fixEnumsInInheritanceHierarchy(@NonNull final Map<String, ModelsMap> allModelMaps) {}
}

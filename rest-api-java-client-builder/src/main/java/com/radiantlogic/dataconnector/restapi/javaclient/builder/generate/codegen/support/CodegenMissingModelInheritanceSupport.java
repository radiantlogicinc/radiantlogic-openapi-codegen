package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support;

import java.util.Map;
import lombok.NonNull;
import org.openapitools.codegen.CodegenModel;

/**
 * There are some scenarios where inheritance relationships that should exist between schemas don't
 * in an OpenAPI spec. This is mainly problematic for classes with inheritance that have fields with
 * inheritance. Take this example:
 *
 * <p>``` public class Group { // ... }
 *
 * <p>public class Team extends Group { // ... }
 *
 * <p>public class Parent { private Group item; }
 *
 * <p>public class Child extends Parent { private Team item; } ```
 *
 * <p>As long as Team extends Group, everything will work. If Team does not extend Group, there will
 * be a compile error. This support class exists to make sure that this kind of hierarchy will
 * always work.
 */
public class CodegenMissingModelInheritanceSupport {
  public void fixMissingModelInheritance(@NonNull final Map<String, CodegenModel> allModels) {}
}

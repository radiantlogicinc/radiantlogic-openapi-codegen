package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support;

/**
 * One of the most painful areas of the codegen is related to enums. The default code generation
 * settings would produce constant compile errors due to inline enums in a class clashing with
 * inline enums in its sub-classes. There are a variety of other support operations designed to
 * correct this. This support class is intended to be run at the end of all of them to simply rip
 * out any inline enums in inheritance hierarchies that remain since the number of permutations of
 * this problem are extensive. At some point, it's better to rip them out and achieve stability
 * rather than continue trying to correct every last use case.
 *
 * <p>In the future, I'm sure more enhancements can be done to further close the gap and reduce the
 * need for this class.
 */
public class CodegenRemoveInheritanceEnumsSupport {
  public void removeInheritedEnums() {}
}

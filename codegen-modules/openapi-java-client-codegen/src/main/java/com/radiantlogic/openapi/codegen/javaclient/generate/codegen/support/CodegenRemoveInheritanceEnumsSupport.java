package com.radiantlogic.openapi.codegen.javaclient.generate.codegen.support;

import java.util.Map;
import lombok.NonNull;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenProperty;

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
  public void removeInheritedEnums(@NonNull final Map<String, CodegenModel> allModels) {
    allModels.values().forEach(model -> removeEnumIfNotEnumInParent(model, model.parentModel));
  }

  private void removeEnumIfNotEnumInParent(
      @NonNull final CodegenModel model, final CodegenModel parentModel) {
    if (parentModel == null) {
      return;
    }

    model.vars.stream()
        .filter(var -> var.isEnum)
        .forEach(var -> removeEnumIfNotInParent(var, parentModel));

    removeEnumIfNotEnumInParent(model, parentModel.parentModel);
  }

  private void removeEnumIfNotInParent(
      @NonNull final CodegenProperty var, @NonNull final CodegenModel parentModel) {
    parentModel.vars.stream()
        .filter(v -> v.name.equals(var.name))
        .findFirst()
        .filter(parentVar -> !parentVar.isEnum)
        .ifPresent(parentVar -> removeChildEnum(var, parentVar));
  }

  private void removeChildEnum(final CodegenProperty childVar, final CodegenProperty parentVar) {
    childVar.isEnum = false;
    childVar.dataType = parentVar.dataType;
    childVar.datatypeWithEnum = parentVar.datatypeWithEnum;
    childVar.openApiType = parentVar.openApiType;
    childVar.allowableValues = parentVar.allowableValues;
    childVar._enum = parentVar._enum;
    childVar.defaultValue = parentVar.defaultValue;
  }
}

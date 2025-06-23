package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support;

import com.radiantlogic.dataconnector.restapi.javaclient.builder.exceptions.ModelNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
 *
 * <p>Due to the high risk of this manipulation, there are many checks performed prior to making the
 * change.
 */
public class CodegenMissingModelInheritanceSupport {
  public void fixInheritanceAllModels(@NonNull final Map<String, CodegenModel> allModels) {
    allModels.values().forEach(model -> fixInheritanceInModel(model, allModels));
  }

  private String getInterfaceNameIfOnlyOne(final List<String> interfaces) {
    return Optional.ofNullable(interfaces)
        .filter(list -> list.size() == 1)
        .map(List::getFirst)
        .orElse(null);
  }

  private String getAllOfNameIfOnlyOne(final Set<String> allOf) {
    return Optional.ofNullable(allOf).filter(set -> set.size() == 1).stream()
        .flatMap(Set::stream)
        .findFirst()
        .orElse(null);
  }

  private void fixInheritanceInModel(
      @NonNull final CodegenModel model, @NonNull final Map<String, CodegenModel> allModels) {
    if (model.parent != null
        || model.dataType == null
        || model.dataType.equals(model.classname)
        || model.isEnum) {
      return;
    }

    final String modelInterface = getInterfaceNameIfOnlyOne(model.interfaces);
    final String modelAllOf = getAllOfNameIfOnlyOne(model.allOf);
    if (modelInterface == null || modelAllOf == null) {
      return;
    }

    if (!modelInterface.equals(modelAllOf) || !modelInterface.equals(model.dataType)) {
      return;
    }

    model.parent = modelInterface;
    final CodegenModel parentModel = allModels.get(modelInterface);
    if (parentModel == null) {
      throw new ModelNotFoundException(
          "Parent model should exist but was not found: %s".formatted(modelInterface));
    }
    model.parentModel = parentModel;
  }
}

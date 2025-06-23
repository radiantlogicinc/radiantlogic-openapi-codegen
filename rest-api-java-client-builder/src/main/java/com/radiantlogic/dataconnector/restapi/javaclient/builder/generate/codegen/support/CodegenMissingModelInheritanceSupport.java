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
 */
public class CodegenMissingModelInheritanceSupport {
  public void fixMissingModelInheritance(@NonNull final Map<String, CodegenModel> allModels) {
    allModels
        .values()
        .forEach(
            model -> {
              if (model.parent != null
                  || model.dataType == null
                  || model.dataType.equals(model.classname)
                  || model.isEnum) {
                return;
              }

              final String modelInterface =
                  Optional.ofNullable(model.interfaces)
                      .filter(list -> list.size() == 1)
                      .map(List::getFirst)
                      .orElse(null);
              final String modelAllOf =
                  Optional.ofNullable(model.allOf).filter(set -> set.size() == 1).stream()
                      .flatMap(Set::stream)
                      .findFirst()
                      .orElse(null);

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
            });
  }
}

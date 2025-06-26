package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support;

import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.utils.CodegenEnumModelUtils;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.utils.CodegenModelUtils;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.utils.CodegenPropertyUtils;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.NonNull;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenProperty;

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
  public record ExtractedEnumModels(
      @NonNull List<CodegenModel> enumsFromModelsWithParents,
      @NonNull List<CodegenModel> enumsFromDiscriminatorParentModels,
      @NonNull List<CodegenModel> enumsFromModelsWithNonDiscriminatorChildren) {}

  public ExtractedEnumModels fixAndExtractInheritedEnums(
      @NonNull final Map<String, CodegenModel> allModels) {

    // The ordering here is important as subsequent passes through the models assume any enums that
    // meet the prior criteria have been removed
    final List<CodegenModel> enumsFromModelsWithParents =
        fixAndExtractEnumsFromAllModelsWithParents(allModels.values());
    final List<CodegenModel> enumsFromDiscriminatorParentModels =
        fixAndExtractEnumsFromAllDiscriminatedUnionModels(allModels);
    final List<CodegenModel> enumsFromModelsWithNonDiscriminatorChildren =
        fixAndExtractEnumsFromAllNonDiscriminatorModelsWithChildren(allModels.values());
    return new ExtractedEnumModels(
        enumsFromModelsWithParents,
        enumsFromDiscriminatorParentModels,
        enumsFromModelsWithNonDiscriminatorChildren);
  }

  // TODO document this
  private static List<CodegenModel> fixAndExtractEnumsFromAllNonDiscriminatorModelsWithChildren(
      @NonNull final Collection<CodegenModel> allModels) {
    return allModels.stream()
        .filter(CodegenModelUtils::hasNonDiscriminatorChildren)
        .flatMap(model -> fixAndExtractEnumsFromNonDiscriminatorModelWithChildren(model, allModels))
        .toList();
  }

  private static Stream<CodegenModel> fixAndExtractEnumsFromNonDiscriminatorModelWithChildren(
      @NonNull final CodegenModel model, @NonNull final Collection<CodegenModel> allModels) {
    return model.vars.stream()
        .filter(CodegenPropertyUtils::isEnumProperty)
        .map(
            var -> {
              setEnumRefProps(var);
              model.oneOf.forEach(
                  childModelName -> {
                    allModels.stream()
                        .filter(m -> m.name.equals(childModelName))
                        .findFirst()
                        .ifPresent(childModel -> ensureChildModelHasNoInlineEnums(var, childModel));
                  });
              return CodegenEnumModelUtils.createEnumModelFromEnumProp(var);
            });
  }

  /**
   * Find all models that are discriminated unions, ie they have oneOf children and a discriminator
   * with a mapping. Then make sure that any properties the parent and all its children have that
   * are enums are separate models (no inline) and are extracted if not. Both parent and child will
   * have that property point to the extracted enum model.
   */
  private static List<CodegenModel> fixAndExtractEnumsFromAllDiscriminatedUnionModels(
      @NonNull final Map<String, CodegenModel> allModels) {
    return allModels.values().stream()
        .filter(CodegenModelUtils::hasDiscriminatorChildren)
        .flatMap(model -> fixAndExtractEnumsFromDiscriminatedUnionModel(model, allModels))
        .toList();
  }

  private static Stream<CodegenModel> fixAndExtractEnumsFromDiscriminatedUnionModel(
      @NonNull final CodegenModel model, @NonNull final Map<String, CodegenModel> allModels) {
    return model.vars.stream()
        .filter(CodegenPropertyUtils::isEnumProperty)
        .map(
            var -> {
              setEnumRefProps(var);
              model
                  .discriminator
                  .getMappedModels()
                  .forEach(
                      mappedModel -> {
                        final CodegenModel childModel = allModels.get(mappedModel.getModelName());
                        ensureChildModelHasNoInlineEnums(var, childModel);
                      });
              return CodegenEnumModelUtils.createEnumModelFromEnumProp(var);
            });
  }

  /**
   * Any models that have a parent model, all properties will be checked and if any are enums are
   * separate models (no inline) and are extracted to be a separate model if not. Also both the
   * parent and child will have that property point to the extracted enum model.
   */
  private static List<CodegenModel> fixAndExtractEnumsFromAllModelsWithParents(
      @NonNull final Collection<CodegenModel> allModels) {
    return allModels.stream()
        .filter(model -> model.parentModel != null)
        .flatMap(CodegenInheritedEnumSupport::fixAndExtractEnumsFromModelWithParent)
        .toList();
  }

  private static Stream<CodegenModel> fixAndExtractEnumsFromModelWithParent(
      @NonNull final CodegenModel model) {
    return model.parentModel.vars.stream()
        .filter(CodegenPropertyUtils::isEnumProperty)
        .peek(
            var -> {
              setEnumRefProps(var);
              ensureChildModelHasNoInlineEnums(var, model);
            })
        .map(CodegenEnumModelUtils::createEnumModelFromEnumProp);
  }

  private static void setEnumRefProps(@NonNull final CodegenProperty property) {
    property.isEnum = false;
    property.isInnerEnum = false;
    property.isEnumRef = true;
  }

  private static void ensureChildModelHasNoInlineEnums(
      @NonNull final CodegenProperty parentEnumProperty, @NonNull final CodegenModel childModel) {
    childModel.vars.stream()
        .filter(
            childVar -> CodegenPropertyUtils.isSamePropertyInChild(parentEnumProperty, childVar))
        .findFirst()
        .ifPresent(childVar -> ensureChildModelPropertyNotInnerEnum(parentEnumProperty, childVar));
  }

  private static void ensureChildModelPropertyNotInnerEnum(
      @NonNull final CodegenProperty parentEnumProperty,
      @NonNull final CodegenProperty matchingChildProperty) {
    // If the property is already an enum ref, don't re-assign it
    if (CodegenPropertyUtils.isEnumRefProp(matchingChildProperty)) {
      return;
    }

    setEnumRefProps(matchingChildProperty);
    matchingChildProperty.dataType = parentEnumProperty.dataType;
    matchingChildProperty.datatypeWithEnum = parentEnumProperty.datatypeWithEnum;
    matchingChildProperty.openApiType = parentEnumProperty.openApiType;
  }
}

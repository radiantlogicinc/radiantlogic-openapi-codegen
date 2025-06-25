package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support;

import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.utils.CodegenConstants;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.utils.CodegenModelUtils;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.utils.CodegenPropertyUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
  private static final Pattern LIST_TYPE_PATTERN = Pattern.compile("^List<(.*)>$");
  private static final Pattern QUOTED_STRING_PATTERN = Pattern.compile("^\"(.*)\"$");

  // TODO probably need to return the enums
  public void fixEnumsInInheritanceHierarchy(@NonNull final Map<String, CodegenModel> allModels) {
    // Parent/child should come before discriminator parent/child due to certain edge cases
    // The one that runs first is the one that will modify the children
    final List<CodegenModel> newEnumsFromModelsWithParents =
        handleInheritedEnumsFromModelsWithParents(allModels.values());
    final List<CodegenModel> newEnumsFromDiscriminatorParentModels =
        handleInheritedEnumsFromDiscriminatorParentModels(allModels);
    final List<CodegenModel> newEnumsFromModelsWithNonDiscriminatorChildren =
        handleInheritedEnumsFromModelsWithNonDiscriminatorChildren(allModels.values());
  }

  private List<CodegenModel> handleInheritedEnumsFromModelsWithNonDiscriminatorChildren(
      @NonNull final Collection<CodegenModel> allModels) {
    return allModels.stream()
        .filter(CodegenModelUtils::hasNonDiscriminatorChildren)
        .flatMap(
            model -> {
              // TODO cleanup
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
                                  .ifPresent(
                                      childModel ->
                                          ensureChildModelHasNoInlineEnums(var, childModel));
                            });
                        return createEnumModel(var);
                      });
            })
        .toList();
  }

  private List<CodegenModel> handleInheritedEnumsFromDiscriminatorParentModels(
      @NonNull final Map<String, CodegenModel> allModels) {
    return allModels.values().stream()
        .filter(CodegenModelUtils::hasDiscriminatorChildren)
        .flatMap(
            model ->
                // TODO cleanup
                model.vars.stream()
                    .filter(CodegenPropertyUtils::isEnumProperty)
                    .map(
                        var -> {
                          setEnumRefProps(var);
                          model
                              .discriminator
                              .getMappedModels()
                              .forEach(
                                  mappedModel -> {
                                    final CodegenModel childModel =
                                        allModels.get(mappedModel.getModelName());
                                    ensureChildModelHasNoInlineEnums(var, childModel);
                                  });
                          return createEnumModel(var);
                        }))
        .toList();
  }

  private static List<CodegenModel> handleInheritedEnumsFromModelsWithParents(
      @NonNull final Collection<CodegenModel> allModels) {
    return allModels.stream()
        .filter(model -> model.parentModel != null)
        .flatMap(
            model ->
                // TODO cleanup
                model.parentModel.vars.stream()
                    .filter(CodegenPropertyUtils::isEnumProperty)
                    .peek(
                        var -> {
                          setEnumRefProps(var);
                          ensureChildModelHasNoInlineEnums(var, model);
                        })
                    .map(CodegenInheritedEnumSupport::createEnumModel))
        .toList();
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

  private static CodegenModel createEnumModel(@NonNull final CodegenProperty enumProp) {
    final CodegenModel enumModel = new CodegenModel();

    // TODO cleanup
    final String typeName;
    if (enumProp.openApiType.equals("array")) {
      final Matcher matcher = LIST_TYPE_PATTERN.matcher(enumProp.datatypeWithEnum);
      if (!matcher.matches()) {
        throw new IllegalStateException(
            "Array enum property has a name that doesn't match pattern: %s"
                .formatted(enumProp.datatypeWithEnum));
      }
      typeName = matcher.group(1);
    } else {
      typeName = enumProp.datatypeWithEnum;
    }

    // TODO cleanup
    final List<Object> propAllowableValuesValues =
        Optional.ofNullable(enumProp.allowableValues)
            .map(map -> (List<Object>) map.get(CodegenConstants.VALUES_KEY))
            .orElseGet(List::of);
    final List<Map<String, Object>> propAllowableValuesEnumVars =
        Optional.ofNullable(enumProp.allowableValues)
            .map(map -> (List<Map<String, Object>>) map.get(CodegenConstants.ENUM_VARS_KEY))
            .orElseGet(List::of);

    // TODO cleanup
    final List<Map<String, Object>> enumVars =
        propAllowableValuesEnumVars.stream()
            .map(
                map -> {
                  final Object value = map.get(CodegenConstants.VALUE_KEY);
                  final Map<String, Object> newMap = new HashMap<>();
                  newMap.put(CodegenConstants.NAME_KEY, map.get(CodegenConstants.NAME_KEY));
                  if (value instanceof String stringValue
                      && !QUOTED_STRING_PATTERN.matcher(stringValue).matches()) {
                    newMap.put(CodegenConstants.VALUE_KEY, "\"%s\"".formatted(stringValue));
                  } else {
                    newMap.put(CodegenConstants.VALUE_KEY, value);
                  }
                  newMap.put(CodegenConstants.IS_STRING_KEY, value instanceof String);
                  return newMap;
                })
            .toList();

    // TODO cleanup
    final Map<String, Object> allowableValues =
        Map.of(
            CodegenConstants.VALUES_KEY,
            propAllowableValuesValues,
            CodegenConstants.ENUM_VARS_KEY,
            enumVars);

    enumModel.name = typeName;
    enumModel.classname = typeName;
    enumModel.isEnum = true;
    enumModel.allowableValues = allowableValues;
    enumModel.classFilename = typeName;
    enumModel.dataType = "String";
    return enumModel;
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

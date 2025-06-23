package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen;

import com.radiantlogic.dataconnector.restapi.javaclient.builder.args.Args;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support.CodegenDiscriminatorTypeSupport;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support.CodegenMetadataSupport;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support.CodegenUnsupportedUnionTypeSupport;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.models.ExtendedCodegenMapper;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.models.ExtendedCodegenModel;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.models.ExtendedCodegenProperty;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.factory.Mappers;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenProperty;
import org.openapitools.codegen.languages.JavaClientCodegen;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.ModelsMap;
import org.openapitools.codegen.utils.ModelUtils;

/**
 * A customized version of the default JavaClientCodegen designed to produce the exact artifact
 * style we want.
 */
@RequiredArgsConstructor
public class DataconnectorJavaClientCodegen extends JavaClientCodegen
    implements ExtendedCodegenConfig {
  private static final String ENUM_VARS_KEY = "enumVars";
  private static final String VALUES_KEY = "values";
  private static final String NAME_KEY = "name";
  private static final String VALUE_KEY = "value";
  private static final String IMPORTS_KEY = "imports";
  private static final String IMPORT_KEY = "import";
  private static final String IS_STRING_KEY = "isString";
  private static final Pattern LIST_TYPE_PATTERN = Pattern.compile("^List<(.*)>$");
  private static final Pattern QUOTED_STRING_PATTERN = Pattern.compile("^\"(.*)\"$");
  private static final Pattern NON_ENGLISH_PATTERN = Pattern.compile("[^\\p{ASCII}]");
  private static final Pattern NON_LETTER_PATTERN = Pattern.compile("[\\W0-9]+");

  private static final ExtendedCodegenMapper CODEGEN_MAPPER =
      Mappers.getMapper(ExtendedCodegenMapper.class);

  private final CodegenMetadataSupport codegenMetadataSupport = new CodegenMetadataSupport();
  private final CodegenUnsupportedUnionTypeSupport codegenUnsupportedUnionTypeSupport =
      new CodegenUnsupportedUnionTypeSupport();
  private final CodegenDiscriminatorTypeSupport codegenDiscriminatorTypeSupport =
      new CodegenDiscriminatorTypeSupport();

  @NonNull private final Args args;

  @Override
  @NonNull
  public Set<String> getIgnorePatterns() {
    return Set.of(
        ".travis.yml",
        "gradle/**",
        "build.gradle",
        "build.sbt",
        "git_push.sh",
        "gradle.properties",
        "gradlew",
        "gradlew.bat",
        "settings.gradle",
        "src/main/AndroidManifest.xml",
        "src/test/**");
  }

  public void init(@NonNull final OpenAPI openAPI) {
    final var metadata = codegenMetadataSupport.getMetadata(openAPI, args);

    setOutputDir(metadata.outputDir().toString());
    setGroupId(args.groupId());

    setApiPackage("%s.api".formatted(metadata.basePackage()));
    setModelPackage("%s.model".formatted(metadata.basePackage()));
    setInvokerPackage("%s.invoker".formatted(metadata.basePackage()));
    setArtifactId(metadata.artifactId());
    setArtifactVersion(metadata.version());
    setDisallowAdditionalPropertiesIfNotPresent(false);
    setUseOneOfInterfaces(true);
    additionalProperties.put("useOneOfInterfaces", true);
    setUseOneOfDiscriminatorLookup(true);
    setLegacyDiscriminatorBehavior(true);
    setUseEnumCaseInsensitive(false);
    setOpenApiNullable(false);
    setLicenseName("The Apache Software License, Version 2.0");
    setLicenseUrl("http://www.apache.org/licenses/LICENSE-2.0.txt");

    setTemplateDir("templates");
    setLibrary("resttemplate");
  }

  @Override
  public void postProcessModelProperty(final CodegenModel model, final CodegenProperty property) {
    super.postProcessModelProperty(model, property);
    // This needs to be here because some schemas result in this property being null, but downstream
    // code expects it to be present, and then boom NPE
    if (property.allowableValues == null) {
      property.allowableValues = new HashMap<>();
    }
  }

  @Override
  protected List<Map<String, Object>> buildEnumVars(
      @NonNull final List<Object> values, @NonNull final String dataType) {
    final var enumVars = super.buildEnumVars(values, dataType);
    final boolean useValueOf = !dataType.equals("BigDecimal");

    final var updatedEnumVars =
        enumVars.stream().peek(map -> map.put("useValueOf", useValueOf)).toList();
    return new ArrayList<>(updatedEnumVars);
  }

  private void fixBadLiteralPropertyNames(@NonNull final ExtendedCodegenProperty prop) {
    if (prop.name != null && StringUtils.isNumeric(prop.name)
        || "true".equals(prop.name)
        || "false".equals(prop.name)) {
      prop.jsonName = prop.name;
      prop.name =
          "value%s%s"
              .formatted(String.valueOf(prop.name.charAt(0)).toUpperCase(), prop.name.substring(1));
    }
  }

  @Override
  public CodegenProperty fromProperty(
      @NonNull final String name,
      @NonNull final Schema propertySchema,
      final boolean required,
      final boolean schemaIsFromAdditionalProperties) {
    final CodegenProperty prop =
        super.fromProperty(name, propertySchema, required, schemaIsFromAdditionalProperties);
    final ExtendedCodegenProperty extendedProp = CODEGEN_MAPPER.extendProperty(prop);
    fixBadLiteralPropertyNames(extendedProp);
    return extendedProp;
  }

  // TODO document how insane this is yet it has been seen in sonarqube
  private void fixNonEnglishOperationIds(@NonNull final OpenAPI openAPI) {
    openAPI.getPaths().entrySet().stream()
        .flatMap(pathEntry -> pathEntry.getValue().readOperations().stream())
        .filter(operation -> Objects.nonNull(operation.getOperationId()))
        .forEach(
            operation -> {
              final Matcher matcher = NON_ENGLISH_PATTERN.matcher(operation.getOperationId());
              final String withoutNonEnglish = matcher.replaceAll("");
              final String withoutNonLetter =
                  NON_LETTER_PATTERN.matcher(withoutNonEnglish).replaceAll("");
              if (StringUtils.isNotBlank(withoutNonLetter)) {
                operation.setOperationId(withoutNonEnglish);
              } else {
                operation.setOperationId(null);
              }
            });
  }

  @Override
  public void preprocessOpenAPI(@NonNull final OpenAPI openAPI) {
    super.preprocessOpenAPI(openAPI);
    fixNonEnglishOperationIds(openAPI);
  }

  @Override
  public CodegenModel fromModel(@NonNull final String name, @NonNull final Schema model) {
    final ExtendedCodegenModel result = CODEGEN_MAPPER.extendModel(super.fromModel(name, model));
    codegenDiscriminatorTypeSupport.fixDiscriminatorType(result);

    /*
     * I've tried making this work in the fromProperty method. In theory that's the better place for it,
     * applying the change to one property at a time. However, I get errors there I don't get when I run the code here.
     * At the time of writing I've spent an extensive amount of time on this project and don't have the time to further investigate the discrepancy.
     */
    codegenUnsupportedUnionTypeSupport.fixUnsupportedUnionTypes(result, model, openAPI);

    // The equals method from the codegen labels the "other" object with the variable name 'o'.
    // It is possible for an OpenAPI schema to have a variable named 'o', in which case we get a
    // compiler error
    if (result.classVarName != null) {
      if (result.classVarName.equals("o")) {
        result.equalsClassVarName = "otherO";
      } else {
        result.equalsClassVarName = result.classVarName;
      }
    }

    return result;
  }

  private static CodegenModel createEnumModel(@NonNull final CodegenProperty enumProp) {
    final CodegenModel enumModel = new CodegenModel();

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

    final List<Object> propAllowableValuesValues =
        Optional.ofNullable(enumProp.allowableValues)
            .map(map -> (List<Object>) map.get(VALUES_KEY))
            .orElseGet(List::of);
    final List<Map<String, Object>> propAllowableValuesEnumVars =
        Optional.ofNullable(enumProp.allowableValues)
            .map(map -> (List<Map<String, Object>>) map.get(ENUM_VARS_KEY))
            .orElseGet(List::of);

    final List<Map<String, Object>> enumVars =
        propAllowableValuesEnumVars.stream()
            .map(
                map -> {
                  final Object value = map.get(VALUE_KEY);
                  final Map<String, Object> newMap = new HashMap<>();
                  newMap.put(NAME_KEY, map.get(NAME_KEY));
                  if (value instanceof String stringValue
                      && !QUOTED_STRING_PATTERN.matcher(stringValue).matches()) {
                    newMap.put(VALUE_KEY, "\"%s\"".formatted(stringValue));
                  } else {
                    newMap.put(VALUE_KEY, value);
                  }
                  newMap.put(IS_STRING_KEY, value instanceof String);
                  return newMap;
                })
            .toList();

    final Map<String, Object> allowableValues =
        Map.of(VALUES_KEY, propAllowableValuesValues, ENUM_VARS_KEY, enumVars);

    enumModel.name = typeName;
    enumModel.classname = typeName;
    enumModel.isEnum = true;
    enumModel.allowableValues = allowableValues;
    enumModel.classFilename = typeName;
    enumModel.dataType = "String";
    return enumModel;
  }

  private ModelsMap enumModelToModelsMap(
      @NonNull final CodegenModel enumModel,
      @NonNull final ModelsMap base,
      @NonNull final List<Map<String, String>> importsForEnums) {
    final ModelsMap modelsMap = new ModelsMap();
    modelsMap.putAll(base);
    modelsMap.setImports(importsForEnums);

    final String importPath = toModelImport(enumModel.classname);
    final ModelMap modelMap = new ModelMap();
    modelMap.setModel(enumModel);
    modelMap.put("importPath", importPath);
    modelsMap.setModels(List.of(modelMap));
    return modelsMap;
  }

  private static boolean isEnumProperty(@NonNull final CodegenProperty codegenProperty) {
    return codegenProperty.isEnum || codegenProperty.isEnumRef || codegenProperty.isInnerEnum;
  }

  private static boolean isSamePropertyInChild(
      @NonNull final CodegenProperty parentProperty, @NonNull final CodegenProperty childProperty) {
    return childProperty.baseName.equals(parentProperty.baseName);
  }

  private static void setEnumRefProps(@NonNull final CodegenProperty property) {
    property.isEnum = false;
    property.isInnerEnum = false;
    property.isEnumRef = true;
  }

  private static boolean hasEnumRefProps(@NonNull final CodegenProperty property) {
    return !property.isEnum && !property.isInnerEnum && property.isEnumRef;
  }

  private static void ensureChildModelPropertyNotInnerEnum(
      @NonNull final CodegenProperty parentEnumProperty,
      @NonNull final CodegenProperty matchingChildProperty) {
    // If the property is already an enum ref, don't re-assign it
    if (hasEnumRefProps(matchingChildProperty)) {
      return;
    }

    setEnumRefProps(matchingChildProperty);
    matchingChildProperty.dataType = parentEnumProperty.dataType;
    matchingChildProperty.datatypeWithEnum = parentEnumProperty.datatypeWithEnum;
    matchingChildProperty.openApiType = parentEnumProperty.openApiType;
  }

  private static void ensureChildModelHasNoInlineEnums(
      @NonNull final CodegenProperty parentEnumProperty, @NonNull final CodegenModel childModel) {
    childModel.vars.stream()
        .filter(childVar -> isSamePropertyInChild(parentEnumProperty, childVar))
        .findFirst()
        .ifPresent(childVar -> ensureChildModelPropertyNotInnerEnum(parentEnumProperty, childVar));
  }

  private static boolean hasDiscriminatorChildren(@NonNull final CodegenModel model) {
    return model.discriminator != null && model.discriminator.getMappedModels() != null;
  }

  private static List<CodegenModel> handleInheritedEnumsFromModelsWithParents(
      @NonNull final Collection<CodegenModel> allModels) {
    return allModels.stream()
        .filter(model -> model.parentModel != null)
        .flatMap(
            model ->
                model.parentModel.vars.stream()
                    .filter(DataconnectorJavaClientCodegen::isEnumProperty)
                    .peek(
                        var -> {
                          setEnumRefProps(var);
                          ensureChildModelHasNoInlineEnums(var, model);
                        })
                    .map(DataconnectorJavaClientCodegen::createEnumModel))
        .toList();
  }

  private List<CodegenModel> handleInheritedEnumsFromDiscriminatorParentModels(
      @NonNull final Map<String, CodegenModel> allModels) {
    return allModels.values().stream()
        .filter(DataconnectorJavaClientCodegen::hasDiscriminatorChildren)
        .flatMap(
            model ->
                model.vars.stream()
                    .filter(DataconnectorJavaClientCodegen::isEnumProperty)
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

  private void handleDiscriminatorChildMappingValues(
      @NonNull final Map<String, CodegenModel> allModels) {
    allModels.values().stream()
        .filter(DataconnectorJavaClientCodegen::hasDiscriminatorChildren)
        .forEach(
            model -> {
              model
                  .discriminator
                  .getMappedModels()
                  .forEach(
                      mappedModel -> {
                        final CodegenModel childModel = allModels.get(mappedModel.getModelName());
                        // This is a special extension used in the template to ensure the correct
                        // mapping value in the JsonTypeName annotation
                        childModel.vendorExtensions.put(
                            "x-discriminator-mapping-value", mappedModel.getMappingName());
                      });
            });
  }

  // TODO clean this up
  private static CodegenModel mergeEnumCodegenModels(
      @NonNull final CodegenModel one, @NonNull final CodegenModel two) {
    if (!one.isEnum || !two.isEnum) {
      throw new IllegalArgumentException("Cannot merge non-enum models");
    }
    final var oneEnumVars =
        (Collection<Map<String, Object>>)
            Optional.ofNullable(one.allowableValues.get(ENUM_VARS_KEY)).orElseGet(List::of);
    final var twoEnumVars =
        (Collection<Map<String, Object>>)
            Optional.ofNullable(two.allowableValues.get(ENUM_VARS_KEY)).orElseGet(List::of);
    final Collection<Map<String, Object>> enumVars =
        Stream.of(oneEnumVars.stream(), twoEnumVars.stream())
            .flatMap(Function.identity())
            .collect(Collectors.toMap(map -> map.get(NAME_KEY), Function.identity(), (a, b) -> b))
            .values();
    final var oneValues =
        (List<Object>) Optional.ofNullable(one.allowableValues.get(VALUES_KEY)).orElseGet(List::of);
    final var twoValues =
        (List<Object>) Optional.ofNullable(two.allowableValues.get(VALUES_KEY)).orElseGet(List::of);
    final List<Object> values =
        Stream.of(oneValues.stream(), twoValues.stream())
            .flatMap(Function.identity())
            .distinct()
            .toList();

    one.allowableValues = Map.of(ENUM_VARS_KEY, enumVars, VALUES_KEY, values);
    return one;
  }

  private void addNewEnumModelMaps(
      @NonNull final Map<String, ModelsMap> allModelMaps,
      @NonNull final List<CodegenModel> newEnumsFromParentModels,
      @NonNull final List<CodegenModel> newEnumsFromDiscriminatorParentModels,
      @NonNull final List<CodegenModel> newEnumsFromModelsWithNonDiscriminatorChildren) {
    final ModelsMap enumModelBase =
        allModelMaps.get(allModelMaps.keySet().stream().findFirst().orElseThrow());

    final Map<String, CodegenModel> allNewEnums =
        Stream.of(
                newEnumsFromParentModels.stream(),
                newEnumsFromDiscriminatorParentModels.stream(),
                newEnumsFromModelsWithNonDiscriminatorChildren.stream())
            .flatMap(Function.identity())
            .map(
                newEnum -> {
                  return Optional.ofNullable(allModelMaps.get(newEnum.name))
                      .map(
                          e ->
                              mergeEnumCodegenModels(
                                  ModelUtils.getModelByName(newEnum.name, allModelMaps), newEnum))
                      .orElse(newEnum);
                })
            .collect(
                Collectors.toMap(
                    CodegenModel::getName,
                    Function.identity(),
                    DataconnectorJavaClientCodegen::mergeEnumCodegenModels));

    final List<Map<String, String>> importsForEnums =
        importMapping().entrySet().stream()
            .filter(
                entry ->
                    !entry.getValue().startsWith("org.joda")
                        && !entry.getValue().startsWith("com.google")
                        && !entry.getValue().startsWith("com.radiantlogic")
                        && !entry.getValue().startsWith("io.swagger.annotations"))
            .map(entry -> Map.of("import", entry.getValue()))
            .toList();

    allNewEnums.forEach(
        (key, model) -> {
          allModelMaps.put(key, enumModelToModelsMap(model, enumModelBase, importsForEnums));
        });
  }

  private static boolean hasNonDiscriminatorChildren(@NonNull final CodegenModel model) {
    final boolean hasOneOfChildren = model.oneOf != null && !model.oneOf.isEmpty();
    final boolean hasNoDiscriminatorChildren =
        model.discriminator == null
            || (model.discriminator.getMappedModels() == null
                || model.discriminator.getMappedModels().isEmpty());
    return hasOneOfChildren && hasNoDiscriminatorChildren;
  }

  private List<CodegenModel> handleInheritedEnumsFromModelsWithNonDiscriminatorChildren(
      @NonNull final Collection<CodegenModel> allModels) {
    return allModels.stream()
        .filter(DataconnectorJavaClientCodegen::hasNonDiscriminatorChildren)
        .flatMap(
            model -> {
              return model.vars.stream()
                  .filter(DataconnectorJavaClientCodegen::isEnumProperty)
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

  // TODO document that this manipulation is being done super carefully with all the checks
  private void handleMissingModelInheritance(@NonNull final Map<String, CodegenModel> allModels) {
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
                throw new IllegalStateException(
                    "Parent model should exist but was not found: %s".formatted(modelInterface));
              }
              model.parentModel = parentModel;
            });
  }

  private void removeEnumIfNotEnumInParent(
      @NonNull final CodegenModel model, final CodegenModel parentModel) {
    if (parentModel == null) {
      return;
    }

    model.vars.forEach(
        var -> {
          if (var.isEnum) {
            parentModel.vars.stream()
                .filter(v -> v.name.equals(var.name))
                .findFirst()
                .filter(parentVar -> !parentVar.isEnum)
                .ifPresent(
                    parentVar -> {
                      var.isEnum = false;
                      var.dataType = parentVar.dataType;
                      var.datatypeWithEnum = parentVar.datatypeWithEnum;
                      var.openApiType = parentVar.openApiType;
                      var.allowableValues = parentVar.allowableValues;
                      var._enum = parentVar._enum;
                      var.defaultValue = parentVar.defaultValue;
                    });
          }
        });

    removeEnumIfNotEnumInParent(model, parentModel.parentModel);
  }

  // TODO explain that enums and inheritance are a PITA and any that couldn't be easily corrected
  // are just removed here
  private void handleRemovingUnresolvableInheritanceEnums(
      @NonNull final Map<String, CodegenModel> allModels) {
    allModels.values().forEach(model -> removeEnumIfNotEnumInParent(model, model.parentModel));
  }

  private Map<String, ModelsMap> fixProblematicKeysForFilenames(
      @NonNull final Map<String, ModelsMap> allModelMaps) {

    final Map<String, CodegenModel> allModels = getAllModels(allModelMaps);

    final Map<String, ModelsMap> fixedModelMaps =
        allModelMaps.entrySet().stream()
            .map(
                entry -> {
                  final String fileName = modelFilename("model.mustache", entry.getKey());
                  final String fileBaseName = FilenameUtils.getBaseName(fileName).toLowerCase();

                  return Map.of(fileBaseName, entry);
                })
            .reduce(
                new HashMap<>(),
                (acc, singleEntryMap) -> {
                  final String fileBaseName =
                      singleEntryMap.keySet().stream().findFirst().orElseThrow();
                  final Map.Entry<String, ModelsMap> entry = singleEntryMap.get(fileBaseName);

                  if (!acc.containsKey(fileBaseName)) {
                    acc.put(fileBaseName, entry);
                    return acc;
                  }

                  final CodegenModel model =
                      ModelUtils.getModelByName(entry.getKey(), allModelMaps);
                  int index = 1;
                  while (acc.containsKey(fileBaseName + index)) {
                    index++;
                  }
                  final String suffix = "V%d".formatted(index);
                  final String newFileBaseName = fileBaseName + suffix;
                  final String newKey = entry.getKey() + suffix;
                  final String oldClassName = model.classname;
                  model.classname = model.classname + suffix;
                  model.classFilename = model.classFilename + suffix;
                  model.dataType = model.dataType + suffix;

                  allModels
                      .values()
                      .forEach(
                          otherModel -> {
                            if (otherModel.imports != null
                                && otherModel.imports.contains(oldClassName)) {
                              otherModel.imports.remove(oldClassName);
                              otherModel.imports.add(model.classname);

                              ((List<Map<String, String>>)
                                      allModelMaps.get(otherModel.name).get(IMPORTS_KEY))
                                  .forEach(
                                      importMap -> {
                                        final String importValue = importMap.get(IMPORT_KEY);
                                        if (importValue.endsWith(".%s".formatted(oldClassName))) {
                                          final String newImportValue =
                                              importValue.replaceAll(
                                                  "\\.%s$".formatted(oldClassName),
                                                  ".%s".formatted(model.classname));
                                          importMap.put("import", newImportValue);
                                        }
                                      });
                            }
                          });

                  acc.put(newFileBaseName, Map.entry(newKey, entry.getValue()));
                  return acc;
                })
            .values()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    // The map created by DefaultGenerator is exactly like this, it must be the exact same type with
    // this comparator to work downstream
    final Map<String, ModelsMap> fixedModelMapsWithComparator =
        new TreeMap<>((o1, o2) -> ObjectUtils.compare(toModelName(o1), toModelName(o2)));

    fixedModelMapsWithComparator.putAll(fixedModelMaps);
    return fixedModelMapsWithComparator;
  }

  @Override
  public Map<String, ModelsMap> postProcessAllModels(
      @NonNull final Map<String, ModelsMap> originalAllModelMaps) {
    final Map<String, ModelsMap> allModelMaps =
        fixProblematicKeysForFilenames(originalAllModelMaps);
    final Map<String, CodegenModel> allModels = getAllModels(allModelMaps);

    handleMissingModelInheritance(allModels);

    // Parent/child should come before discriminator parent/child due to certain edge cases
    // The one that runs first is the one that will modify the children
    final List<CodegenModel> newEnumsFromModelsWithParents =
        handleInheritedEnumsFromModelsWithParents(allModels.values());
    final List<CodegenModel> newEnumsFromDiscriminatorParentModels =
        handleInheritedEnumsFromDiscriminatorParentModels(allModels);
    final List<CodegenModel> newEnumsFromModelsWithNonDiscriminatorChildren =
        handleInheritedEnumsFromModelsWithNonDiscriminatorChildren(allModels.values());
    addNewEnumModelMaps(
        allModelMaps,
        newEnumsFromModelsWithParents,
        newEnumsFromDiscriminatorParentModels,
        newEnumsFromModelsWithNonDiscriminatorChildren);

    handleDiscriminatorChildMappingValues(allModels);

    handleRemovingUnresolvableInheritanceEnums(allModels);

    return super.postProcessAllModels(allModelMaps);
  }
}

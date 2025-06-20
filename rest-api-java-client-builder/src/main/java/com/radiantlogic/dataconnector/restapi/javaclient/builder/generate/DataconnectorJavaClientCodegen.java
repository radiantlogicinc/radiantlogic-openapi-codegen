package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate;

import com.radiantlogic.dataconnector.restapi.javaclient.builder.args.Args;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.io.CodegenPaths;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
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
public class DataconnectorJavaClientCodegen extends JavaClientCodegen {
  private static final String ENUM_VARS_KEY = "enumVars";
  private static final String VALUES_KEY = "values";
  private static final String NAME_KEY = "name";
  private static final String VALUE_KEY = "value";
  private static final String IMPORTS_KEY = "imports";
  private static final String IMPORT_KEY = "import";
  private static final String IS_STRING_KEY = "isString";
  private static final Pattern LIST_TYPE_PATTERN = Pattern.compile("^List<(.*)>$");
  private static final Pattern SCHEMA_REF_PATTERN = Pattern.compile("^#/components/schemas/(.*)$");
  private static final Pattern QUOTED_STRING_PATTERN = Pattern.compile("^\"(.*)\"$");

  private static final CodegenPropertyMapper codegenPropertyMapper =
      Mappers.getMapper(CodegenPropertyMapper.class);

  public DataconnectorJavaClientCodegen(@NonNull final OpenAPI openAPI, @NonNull final Args args) {
    setOpenAPI(openAPI);
    init(args);
  }

  public List<String> getIgnorePatterns() {
    return List.of(
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

  private static String ensureValidPackageName(@NonNull final String packageName) {
    return packageName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
  }

  /**
   * The package name generated here must conform to valid java package name rules. If a package
   * element ends up with a leading number, that cannot be allowed.
   */
  private static String fixLeadingNumbers(@NonNull final String packageName) {
    return Arrays.stream(packageName.split("\\."))
        .map(
            name -> {
              final String beginning =
                  switch (name.charAt(0)) {
                    case '0' -> "zero";
                    case '1' -> "one";
                    case '2' -> "two";
                    case '3' -> "three";
                    case '4' -> "four";
                    case '5' -> "five";
                    case '6' -> "six";
                    case '7' -> "seven";
                    case '8' -> "eight";
                    case '9' -> "nine";
                    default -> "%s".formatted(name.charAt(0));
                  };
              return "%s%s".formatted(beginning, name.substring(1));
            })
        .collect(Collectors.joining("."));
  }

  private void init(@NonNull final Args args) {
    final String title = getOpenapiTitle();
    final String version = getOpenapiVersion();
    final Path outputDir = CodegenPaths.OUTPUT_DIR.resolve(title).resolve(version);
    setOutputDir(outputDir.toString());
    setGroupId(args.groupId());

    final String basePackage =
        fixLeadingNumbers("%s.%s".formatted(getGroupId(), ensureValidPackageName(title)));
    setApiPackage("%s.api".formatted(basePackage));
    setModelPackage("%s.model".formatted(basePackage));
    setInvokerPackage("%s.invoker".formatted(basePackage));
    setArtifactId(title);
    setArtifactVersion(version);
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

  // TODO need tests
  private String getOpenapiTitle() {
    return Optional.ofNullable(openAPI.getInfo())
        .map(Info::getTitle)
        .map(title -> title.replaceAll("\\s+", "-").replace("&", ""))
        .orElse("unknown-api");
  }

  // TODO need tests
  private String getOpenapiVersion() {
    return Optional.ofNullable(openAPI.getInfo()).map(Info::getVersion).orElse("unknown-version");
  }

  private boolean isIncorrectlyFlattened(@NonNull final Schema schema) {
    if (schema.getType() != null && schema.getType().equals("object")) {
      return false;
    }

    if (schema.get$ref() != null) {
      final String schemaName = parseSchemaRef(schema.get$ref());
      final Schema refSchema = ModelUtils.getSchema(openAPI, schemaName);
      return isIncorrectlyFlattened(refSchema);
    }

    if (schema.getOneOf() == null && schema.getAnyOf() == null) {
      return false;
    }

    // TODO clean this up
    final long nonObjectOneOfCount =
        Optional.ofNullable((List<Schema>) schema.getOneOf()).stream()
            .flatMap(List::stream)
            .filter(s -> !(s instanceof ObjectSchema))
            .count();

    final long nonObjectAnyOfCount =
        Optional.ofNullable((List<Schema>) schema.getAnyOf()).stream()
            .flatMap(List::stream)
            .filter(s -> !(s instanceof ObjectSchema))
            .count();

    return nonObjectOneOfCount > 0 || nonObjectAnyOfCount > 0;
  }

  private static String parseSchemaRef(final String ref) {
    final Matcher matcher = SCHEMA_REF_PATTERN.matcher(ref);
    if (!matcher.matches()) {
      throw new IllegalStateException("Invalid schema ref: %s".formatted(ref));
    }
    return matcher.group(1);
  }

  private CodegenProperty fixIncorrectComplexType(
      @NonNull final String name,
      @NonNull final CodegenProperty property,
      final Schema modelSchema) {
    final Schema propertySchema =
        Optional.ofNullable((Map<String, Schema>) modelSchema.getProperties())
            .orElseGet(Map::of)
            .get(property.baseName);
    if (propertySchema == null) {
      return property;
    }

    if (!isIncorrectlyFlattened(propertySchema)) {
      return property;
    }

    property.openApiType = "Object";
    property.dataType = "Object";
    property.datatypeWithEnum = "Object";
    property.baseType = "Object";
    property.defaultValue = null;
    return property;
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
      @NonNull final Schema p,
      final boolean required,
      final boolean schemaIsFromAdditionalProperties) {
    final CodegenProperty prop =
        super.fromProperty(name, p, required, schemaIsFromAdditionalProperties);
    final ExtendedCodegenProperty extendedProp = codegenPropertyMapper.extendProperty(prop);
    fixBadLiteralPropertyNames(extendedProp);
    return extendedProp;
  }

  @Override
  public CodegenModel fromModel(@NonNull final String name, @NonNull final Schema model) {
    final CodegenModel result = super.fromModel(name, model);
    if (result.discriminator != null) {
      result.getVars().stream()
          .filter(prop -> prop.getBaseName().equals(result.discriminator.getPropertyBaseName()))
          .findFirst()
          .ifPresent(prop -> result.discriminator.setPropertyType(prop.getDatatypeWithEnum()));
    }

    final List<CodegenProperty> fixedVars =
        result.getVars().stream()
            .map(
                property -> {
                  if (property.getComplexType() != null) {
                    return fixIncorrectComplexType(name, property, model);
                  }
                  return property;
                })
            .toList();
    result.setVars(new ArrayList<>(fixedVars)); // Must be mutable for downstream code

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
    // TODO might want to check what kind of base I'm using here... might not be the best...
    // TODO imports are the issue
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
                        && !entry.getValue().startsWith("com.radiantlogic"))
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

package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate;

import com.radiantlogic.dataconnector.restapi.javaclient.builder.args.Args;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.io.CodegenPaths;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
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
  private static final Pattern LIST_TYPE_PATTERN = Pattern.compile("^List<(.*)>$");
  private static final Pattern SCHEMA_REF_PATTERN = Pattern.compile("^#/components/schemas/(.*)$");

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

  private boolean isIncorrectlyFlattenedRef(@NonNull final Schema schema) {
    if (schema.getOneOf() != null) {}
  }

  private static String parseSchemaRef(final String ref) {
    final Matcher matcher = SCHEMA_REF_PATTERN.matcher(ref);
    if (!matcher.matches()) {
      throw new IllegalStateException("Invalid schema ref: %s".formatted(ref));
    }
    return matcher.group(1);
  }

  private Schema fixIncorrectlyFlattenedProps(
      @NonNull final Schema schema, @NonNull final Map<String, Schema> allSchemas) {
    if (schema.getProperties() == null) {
      return schema;
    }

    final Map<String, Schema> fixedProperties =
        ((Set<Map.Entry<String, Schema>>) schema.getProperties().entrySet())
            .stream()
                .map(
                    entry -> {
                      // TODO what if it was originally like this?
                      if (entry.getValue().get$ref() != null) {
                        final Schema ref =
                            allSchemas.get(parseSchemaRef(entry.getValue().get$ref()));
                        isIncorrectlyFlattenedRef(ref);
                        return Map.entry(entry.getKey(), ref);
                      }
                      return entry;
                    })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    schema.setProperties(fixedProperties);
    return schema;
  }

  @Override
  public void preprocessOpenAPI(@NonNull final OpenAPI openAPI) {
    final Map<String, Schema> schemas =
        Optional.ofNullable(openAPI.getComponents().getSchemas()).orElseGet(Map::of);
    schemas.entrySet().stream()
        .map(
            entry -> {
              if (entry.getValue().getType() == null
                  || entry.getValue().getType().equals("object")) {
                return Map.entry(
                    entry.getKey(), fixIncorrectlyFlattenedProps(entry.getValue(), schemas));
              }
              return entry;
            })
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    openAPI.getComponents().setSchemas(schemas);
    super.preprocessOpenAPI(openAPI);
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

    enumModel.name = typeName;
    enumModel.classname = typeName;
    enumModel.isEnum = true;
    enumModel.allowableValues = enumProp.allowableValues;
    enumModel.classFilename = typeName;
    enumModel.dataType = "String";
    return enumModel;
  }

  private ModelsMap enumModelToModelsMap(
      @NonNull final CodegenModel enumModel, @NonNull final ModelsMap base) {
    final ModelsMap modelsMap = new ModelsMap();
    modelsMap.putAll(base);

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

  private static List<CodegenModel> handleInheritedEnumsFromParentModels(
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
      @NonNull final Collection<CodegenModel> allModels,
      @NonNull final Map<String, ModelsMap> allModelMaps) {
    return allModels.stream()
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
                                        ModelUtils.getModelByName(
                                            mappedModel.getModelName(), allModelMaps);
                                    ensureChildModelHasNoInlineEnums(var, childModel);
                                  });
                          return createEnumModel(var);
                        }))
        .toList();
  }

  private void handleDiscriminatorChildMappingValues(
      @NonNull final Collection<CodegenModel> allModels,
      @NonNull final Map<String, ModelsMap> allModelMaps) {
    allModels.stream()
        .filter(DataconnectorJavaClientCodegen::hasDiscriminatorChildren)
        .forEach(
            model -> {
              model
                  .discriminator
                  .getMappedModels()
                  .forEach(
                      mappedModel -> {
                        final CodegenModel childModel =
                            ModelUtils.getModelByName(mappedModel.getModelName(), allModelMaps);
                        // This is a special extension used in the template to ensure the correct
                        // mapping value in the JsonTypeName annotation
                        childModel.vendorExtensions.put(
                            "x-discriminator-mapping-value", mappedModel.getMappingName());
                      });
            });
  }

  private void addNewEnumModelMaps(
      @NonNull final Map<String, ModelsMap> allModelMaps,
      @NonNull final List<CodegenModel> newEnumsFromParentModels,
      @NonNull final List<CodegenModel> newEnumsFromDiscriminatorParentModels) {
    final ModelsMap enumModelBase =
        allModelMaps.get(allModelMaps.keySet().stream().findFirst().orElseThrow());
    final Map<String, ModelsMap> allNewEnumModels =
        Stream.concat(
                newEnumsFromParentModels.stream(), newEnumsFromDiscriminatorParentModels.stream())
            .collect(
                Collectors.toMap(
                    CodegenModel::getClassname,
                    enumModel -> enumModelToModelsMap(enumModel, enumModelBase),
                    // If there are two duplicate keys, they are duplicate models so handle it
                    // gracefully
                    (a, b) -> b));
    allModelMaps.putAll(allNewEnumModels);
  }

  @Override
  public Map<String, ModelsMap> postProcessAllModels(
      @NonNull final Map<String, ModelsMap> allModelMaps) {
    final Collection<CodegenModel> allModels = getAllModels(allModelMaps).values();

    // Parent/child should come before discriminator parent/child due to certain edge cases
    // The one that runs first is the one that will modify the children
    final List<CodegenModel> newEnumsFromParentModels =
        handleInheritedEnumsFromParentModels(allModels);
    final List<CodegenModel> newEnumsFromDiscriminatorParentModels =
        handleInheritedEnumsFromDiscriminatorParentModels(allModels, allModelMaps);
    addNewEnumModelMaps(
        allModelMaps, newEnumsFromParentModels, newEnumsFromDiscriminatorParentModels);

    handleDiscriminatorChildMappingValues(allModels, allModelMaps);

    return super.postProcessAllModels(allModelMaps);
  }
}

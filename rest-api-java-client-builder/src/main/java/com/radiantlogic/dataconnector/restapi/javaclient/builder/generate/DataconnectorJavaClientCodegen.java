package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate;

import com.radiantlogic.dataconnector.restapi.javaclient.builder.args.Args;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.io.CodegenPaths;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
@Slf4j
public class DataconnectorJavaClientCodegen extends JavaClientCodegen {
  private static final Pattern LIST_TYPE_PATTERN = Pattern.compile("^List<(.*)>$");

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

  private void init(@NonNull final Args args) {
    final String title = getOpenapiTitle();
    final String version = getOpenapiVersion();
    final Path outputDir = CodegenPaths.OUTPUT_DIR.resolve(title).resolve(version);
    setOutputDir(outputDir.toString());
    setGroupId(args.groupId());
    final String basePackage = "%s.%s".formatted(getGroupId(), ensureValidPackageName(title));
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
    // code expects
    // it to be present, and then boom NPE
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

  // TODO copied from parent
  protected void updateModelForComposedSchema(
      CodegenModel m, Schema schema, Map<String, Schema> allDefinitions) {
    final Schema composed = schema;
    Map<String, Schema> properties = new LinkedHashMap<>();
    List<String> required = new ArrayList<>();
    Map<String, Schema> allProperties = new LinkedHashMap<>();
    List<String> allRequired = new ArrayList<>();

    // if schema has properties outside of allOf/oneOf/anyOf also add them to m
    if (composed.getProperties() != null && !composed.getProperties().isEmpty()) {
      if (composed.getOneOf() != null && !composed.getOneOf().isEmpty()) {
        log.warn(
            "'oneOf' is intended to include only the additional optional OAS extension discriminator object. "
                + "For more details, see https://json-schema.org/draft/2019-09/json-schema-core.html#rfc.section.9.2.1.3 and the OAS section on 'Composition and Inheritance'.");
      }
      addVars(
          m, unaliasPropertySchema(composed.getProperties()), composed.getRequired(), null, null);
    }

    // parent model
    final String parentName = ModelUtils.getParentName(composed, allDefinitions);
    final List<String> allParents = ModelUtils.getAllParentsName(composed, allDefinitions, false);
    final Schema parent =
        StringUtils.isBlank(parentName) || allDefinitions == null
            ? null
            : allDefinitions.get(parentName);

    // TODO revise the logic below to set discriminator, xml attributes
    if (supportsInheritance || supportsMixins) {
      m.allVars = new ArrayList<>();
      if (composed.getAllOf() != null) {
        int modelImplCnt = 0; // only one inline object allowed in a ComposedModel
        int modelDiscriminators = 0; // only one discriminator allowed in a ComposedModel
        for (Object innerSchema :
            composed.getAllOf()) { // TODO need to work with anyOf, oneOf as well
          if (m.discriminator == null && ((Schema) innerSchema).getDiscriminator() != null) {
            log.debug("discriminator is set to null (not correctly set earlier): {}", m.name);
            m.setDiscriminator(createDiscriminator(m.name, (Schema) innerSchema));
            modelDiscriminators++;
          }

          if (((Schema) innerSchema).getXml() != null) {
            m.xmlPrefix = ((Schema) innerSchema).getXml().getPrefix();
            m.xmlNamespace = ((Schema) innerSchema).getXml().getNamespace();
            m.xmlName = ((Schema) innerSchema).getXml().getName();
          }
          if (modelDiscriminators > 1) {
            log.debug(
                "Allof composed schema is inheriting >1 discriminator. Only use one discriminator: {}",
                composed);
          }

          if (modelImplCnt++ > 1) {
            log.debug(
                "More than one inline schema specified in allOf:. Only the first one is recognized. All others are ignored.");
            break; // only one schema with discriminator allowed in allOf
          }
        }
      }
    }

    // interfaces (schemas defined in allOf, anyOf, oneOf)
    List<Schema> interfaces = ModelUtils.getInterfaces(composed);
    if (!interfaces.isEmpty()) {
      // m.interfaces is for backward compatibility
      if (m.interfaces == null) m.interfaces = new ArrayList<>();

      for (Schema interfaceSchema : interfaces) {
        interfaceSchema = unaliasSchema(interfaceSchema);

        if (StringUtils.isBlank(interfaceSchema.get$ref())) {
          // primitive type
          String languageType = getTypeDeclaration(interfaceSchema);
          CodegenProperty interfaceProperty = fromProperty(languageType, interfaceSchema, false);
          if (ModelUtils.isArraySchema(interfaceSchema)
              || ModelUtils.isMapSchema(interfaceSchema)) {
            while (interfaceProperty != null) {
              addImport(m, interfaceProperty.complexType);
              interfaceProperty = interfaceProperty.items;
            }
          }

          if (composed.getAnyOf() != null) {
            if (m.anyOf.contains(languageType)) {
              log.debug(
                  "{} (anyOf schema) already has `{}` defined and therefore it's skipped.",
                  m.name,
                  languageType);
            } else {
              m.anyOf.add(languageType);
            }
          } else if (composed.getOneOf() != null) {
            if (m.oneOf.contains(languageType)) {
              log.debug(
                  "{} (oneOf schema) already has `{}` defined and therefore it's skipped.",
                  m.name,
                  languageType);
            } else {
              m.oneOf.add(languageType);
            }
          } else if (composed.getAllOf() != null) {
            // no need to add primitive type to allOf, which should comprise of schemas (models)
            // only
          } else {
            log.error("Composed schema has incorrect anyOf, allOf, oneOf defined: {}", composed);
          }
          continue;
        }

        // the rest of the section is for model
        Schema refSchema = null;
        String ref = ModelUtils.getSimpleRef(interfaceSchema.get$ref());
        if (allDefinitions != null) {
          refSchema = allDefinitions.get(ref);
        }
        final String modelName = toModelName(ref);
        CodegenProperty interfaceProperty = fromProperty(modelName, interfaceSchema, false);
        m.interfaces.add(modelName);
        addImport(composed, refSchema, m, modelName);

        if (allDefinitions != null && refSchema != null) {
          if (allParents.contains(ref) && supportsMultipleInheritance) {
            // multiple inheritance
            addProperties(allProperties, allRequired, refSchema, new HashSet<>());
          } else if (parentName != null && parentName.equals(ref) && supportsInheritance) {
            // single inheritance
            addProperties(allProperties, allRequired, refSchema, new HashSet<>());
          } else {
            // composition
            Map<String, Schema> newProperties = new LinkedHashMap<>();
            addProperties(newProperties, required, refSchema, new HashSet<>());
            mergeProperties(properties, newProperties);
            addProperties(allProperties, allRequired, refSchema, new HashSet<>());
          }
        }

        if (composed.getAnyOf() != null) {
          m.anyOf.add(modelName);
        } else if (composed.getOneOf() != null) {
          m.oneOf.add(modelName);
          if (!m.permits.contains(modelName)) {
            m.permits.add(modelName);
          }
        } else if (composed.getAllOf() != null) {
          m.allOf.add(modelName);
        } else {
          log.error("Composed schema has incorrect anyOf, allOf, oneOf defined: {}", composed);
        }
      }
    }

    if (parent != null && composed.getAllOf() != null) { // set parent for allOf only
      m.parentSchema = parentName;
      m.parent = toModelName(parentName);

      if (supportsMultipleInheritance) {
        m.allParents = new ArrayList<>();
        for (String pname : allParents) {
          String pModelName = toModelName(pname);
          m.allParents.add(pModelName);
          addImport(m, pModelName);
        }
      } else { // single inheritance
        addImport(m, m.parent);
      }
    }

    // child schema (properties owned by the schema itself)
    for (Schema component : interfaces) {
      if (component.get$ref() == null) {
        if (component != null) {
          // component is the child schema
          addProperties(properties, required, component, new HashSet<>());

          // includes child's properties (all, required) in allProperties, allRequired
          addProperties(allProperties, allRequired, component, new HashSet<>());
        }
        // in 7.0.0 release, we comment out below to allow more than 1 child schemas in allOf
        // break; // at most one child only
      }
    }

    if (composed.getRequired() != null) {
      required.addAll(composed.getRequired());
      allRequired.addAll(composed.getRequired());
    }

    addVars(
        m,
        unaliasPropertySchema(properties),
        required,
        unaliasPropertySchema(allProperties),
        allRequired);

    // Per OAS specification, composed schemas may use the 'additionalProperties' keyword.
    if (supportsAdditionalPropertiesWithComposedSchema) {
      // Process the schema specified with the 'additionalProperties' keyword.
      // This will set the 'CodegenModel.additionalPropertiesType' field
      // and potentially 'Codegen.parent'.
      //
      // Note: it's not a good idea to use single class inheritance to implement
      // the 'additionalProperties' keyword. Code generators that use single class
      // inheritance sometimes use the 'Codegen.parent' field to implement the
      // 'additionalProperties' keyword. However, that would be in conflict with
      // 'allOf' composed schemas, because these code generators also want to set
      // 'Codegen.parent' to the first child schema of the 'allOf' schema.
      addAdditionPropertiesToCodeGenModel(m, schema);
    }

    if (Boolean.TRUE.equals(schema.getNullable())) {
      m.isNullable = Boolean.TRUE;
    }

    // end of code block for composed schema
  }

  // TODO copied from parent
  private void mergeProperties(
      Map<String, Schema> existingProperties, Map<String, Schema> newProperties) {
    // https://github.com/OpenAPITools/openapi-generator/issues/12545
    if (null != existingProperties && null != newProperties) {
      Schema existingType = existingProperties.get("type");
      Schema newType = newProperties.get("type");
      newProperties.forEach(
          (key, value) ->
              existingProperties.put(
                  key, ModelUtils.cloneSchema(value, specVersionGreaterThanOrEqualTo310(openAPI))));
      if (null != existingType
          && null != newType
          && null != newType.getEnum()
          && !newType.getEnum().isEmpty()) {
        for (Object e : newType.getEnum()) {
          // ensure all interface enum types are added to schema
          if (null != existingType.getEnum() && !existingType.getEnum().contains(e)) {
            existingType.addEnumItemObject(e);
          }
        }
        existingProperties.put("type", existingType);
      }
    }
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

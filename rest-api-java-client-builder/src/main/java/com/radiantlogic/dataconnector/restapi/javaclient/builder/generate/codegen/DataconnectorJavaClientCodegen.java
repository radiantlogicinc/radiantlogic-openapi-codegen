package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen;

import com.radiantlogic.dataconnector.restapi.javaclient.builder.args.Args;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support.CodegenDiscriminatorSupport;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support.CodegenEnumValueOfSupport;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support.CodegenFilenameSupport;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support.CodegenInheritedEnumSupport;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support.CodegenLiteralPropertyNameSupport;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support.CodegenMetadataSupport;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support.CodegenMissingModelInheritanceSupport;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support.CodegenNonEnglishNameSupport;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support.CodegenRemoveInheritanceEnumsSupport;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support.CodegenUnsupportedUnionTypeSupport;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.utils.CodegenModelUtils;
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
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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

  private static final ExtendedCodegenMapper CODEGEN_MAPPER =
      Mappers.getMapper(ExtendedCodegenMapper.class);

  private final CodegenMetadataSupport codegenMetadataSupport = new CodegenMetadataSupport();
  private final CodegenUnsupportedUnionTypeSupport codegenUnsupportedUnionTypeSupport =
      new CodegenUnsupportedUnionTypeSupport();
  private final CodegenDiscriminatorSupport codegenDiscriminatorSupport =
      new CodegenDiscriminatorSupport();
  private final CodegenNonEnglishNameSupport codegenNonEnglishNameSupport =
      new CodegenNonEnglishNameSupport();
  private final CodegenEnumValueOfSupport codegenEnumValueOfSupport =
      new CodegenEnumValueOfSupport();
  private final CodegenLiteralPropertyNameSupport codegenLiteralPropertyNameSupport =
      new CodegenLiteralPropertyNameSupport();
  private final CodegenMissingModelInheritanceSupport codegenMissingModelInheritanceSupport =
      new CodegenMissingModelInheritanceSupport();
  private final CodegenRemoveInheritanceEnumsSupport codegenRemoveInheritanceEnumsSupport =
      new CodegenRemoveInheritanceEnumsSupport();
  private final CodegenFilenameSupport codegenFilenameSupport = new CodegenFilenameSupport();
  private final CodegenInheritedEnumSupport codegenInheritedEnumSupport =
      new CodegenInheritedEnumSupport();

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
    final var updatedEnumVars = codegenEnumValueOfSupport.fixValueOfInEnumVars(enumVars, dataType);
    // Must be mutable for downstream
    return new ArrayList<>(updatedEnumVars);
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
    codegenLiteralPropertyNameSupport.fixBadNames(extendedProp);
    return extendedProp;
  }

  @Override
  public void preprocessOpenAPI(@NonNull final OpenAPI openAPI) {
    super.preprocessOpenAPI(openAPI);
    codegenNonEnglishNameSupport.fixOperationIds(openAPI);
  }

  @Override
  public CodegenModel fromModel(@NonNull final String name, @NonNull final Schema model) {
    final ExtendedCodegenModel result = CODEGEN_MAPPER.extendModel(super.fromModel(name, model));
    codegenDiscriminatorSupport.fixDiscriminatorType(result);

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

  // TODO delete this
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
    final ModelsMap rawEnumModelBase =
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

    final ModelsMap enumModelBase = new ModelsMap();
    enumModelBase.putAll(rawEnumModelBase);
    enumModelBase.setImports(importsForEnums);

    allNewEnums.forEach(
        (key, model) -> {
          allModelMaps.put(
              key, CodegenModelUtils.wrapInModelsMap(enumModelBase, modelPackage(), model));
        });
  }

  @Override
  public Map<String, ModelsMap> postProcessAllModels(
      @NonNull final Map<String, ModelsMap> allModelMaps) {
    codegenFilenameSupport.fixProblematicKeysForFilenames(allModelMaps, this::modelFilename);
    final Map<String, CodegenModel> allModels = getAllModels(allModelMaps);

    codegenMissingModelInheritanceSupport.fixInheritanceAllModels(allModels);

    final CodegenInheritedEnumSupport.ExtractedEnumModels extractedEnumModels =
        codegenInheritedEnumSupport.fixEnumsInInheritanceHierarchy(allModels);

    addNewEnumModelMaps(
        allModelMaps,
        extractedEnumModels.newEnumsFromModelsWithParents(),
        extractedEnumModels.newEnumsFromDiscriminatorParentModels(),
        extractedEnumModels.newEnumsFromModelsWithNonDiscriminatorChildren());

    codegenDiscriminatorSupport.fixAllDiscriminatorMappings(allModels);

    codegenRemoveInheritanceEnumsSupport.removeInheritedEnums(allModels);

    return super.postProcessAllModels(allModelMaps);
  }
}

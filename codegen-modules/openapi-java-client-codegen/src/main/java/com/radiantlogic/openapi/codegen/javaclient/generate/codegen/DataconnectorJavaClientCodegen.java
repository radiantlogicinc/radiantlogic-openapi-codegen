package com.radiantlogic.openapi.codegen.javaclient.generate.codegen;

import com.radiantlogic.openapi.codegen.javaclient.args.Args;
import com.radiantlogic.openapi.codegen.javaclient.generate.codegen.support.CodegenDiscriminatorSupport;
import com.radiantlogic.openapi.codegen.javaclient.generate.codegen.support.CodegenEnumValueOfSupport;
import com.radiantlogic.openapi.codegen.javaclient.generate.codegen.support.CodegenFilenameSupport;
import com.radiantlogic.openapi.codegen.javaclient.generate.codegen.support.CodegenInheritedEnumSupport;
import com.radiantlogic.openapi.codegen.javaclient.generate.codegen.support.CodegenLiteralPropertyNameSupport;
import com.radiantlogic.openapi.codegen.javaclient.generate.codegen.support.CodegenMetadataSupport;
import com.radiantlogic.openapi.codegen.javaclient.generate.codegen.support.CodegenMissingModelInheritanceSupport;
import com.radiantlogic.openapi.codegen.javaclient.generate.codegen.support.CodegenNewEnumProcessorSupport;
import com.radiantlogic.openapi.codegen.javaclient.generate.codegen.support.CodegenNonEnglishNameSupport;
import com.radiantlogic.openapi.codegen.javaclient.generate.codegen.support.CodegenRemoveInheritanceEnumsSupport;
import com.radiantlogic.openapi.codegen.javaclient.generate.codegen.support.CodegenUnsupportedUnionTypeSupport;
import com.radiantlogic.openapi.codegen.javaclient.generate.codegen.support.ExtractedEnumModels;
import com.radiantlogic.openapi.codegen.javaclient.generate.models.ExtendedCodegenMapper;
import com.radiantlogic.openapi.codegen.javaclient.generate.models.ExtendedCodegenModel;
import com.radiantlogic.openapi.codegen.javaclient.generate.models.ExtendedCodegenProperty;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenProperty;
import org.openapitools.codegen.languages.JavaClientCodegen;
import org.openapitools.codegen.model.ModelsMap;

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
  private final CodegenNewEnumProcessorSupport codegenNewEnumProcessorSupport =
      new CodegenNewEnumProcessorSupport();

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

  @Override
  public Map<String, ModelsMap> postProcessAllModels(
      @NonNull final Map<String, ModelsMap> allModelMaps) {
    codegenFilenameSupport.fixProblematicKeysForFilenames(allModelMaps, this::modelFilename);
    final Map<String, CodegenModel> allModels = getAllModels(allModelMaps);

    codegenMissingModelInheritanceSupport.fixInheritanceAllModels(allModels);
    final ExtractedEnumModels extractedEnumModels =
        codegenInheritedEnumSupport.fixAndExtractInheritedEnums(allModels);
    codegenNewEnumProcessorSupport.processNewEnumsAndMergeToModelMaps(
        extractedEnumModels.allEnums(), allModelMaps, modelPackage(), importMapping());
    codegenDiscriminatorSupport.fixAllDiscriminatorMappings(allModels);
    codegenRemoveInheritanceEnumsSupport.removeInheritedEnums(allModels);

    return super.postProcessAllModels(allModelMaps);
  }
}

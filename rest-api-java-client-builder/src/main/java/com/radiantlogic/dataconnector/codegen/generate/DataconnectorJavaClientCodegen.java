package com.radiantlogic.dataconnector.codegen.generate;

import com.radiantlogic.dataconnector.codegen.args.Args;
import com.radiantlogic.dataconnector.codegen.io.CodegenPaths;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.apache.commons.io.FileUtils;
import org.openapitools.codegen.CodegenDiscriminator;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenProperty;
import org.openapitools.codegen.languages.JavaClientCodegen;
import org.openapitools.codegen.model.ModelsMap;
import org.openapitools.codegen.utils.ModelUtils;

/**
 * A customized version of the default JavaClientCodegen designed to produce the exact artifact
 * style we want.
 */
public class DataconnectorJavaClientCodegen extends JavaClientCodegen {
  public DataconnectorJavaClientCodegen(@NonNull final OpenAPI openAPI, @NonNull final Args args) {
    setOpenAPI(openAPI);
    init(args);
  }

  private void init(@NonNull final Args args) {
    final String title = getOpenapiTitle();
    final String version = getOpenapiVersion();
    final Path outputDir = CodegenPaths.OUTPUT_DIR.resolve(title).resolve(version);
    if (Files.exists(outputDir)) {
      try {
        FileUtils.deleteDirectory(outputDir.toFile());
      } catch (IOException ex) {
        throw new RuntimeException(
            "Unable to delete existing output directory: %s".formatted(outputDir), ex);
      }
    }
    setOutputDir(outputDir.toString());
    setGroupId(args.groupId());
    // TODO need to validate groupId
    setApiPackage("%s.api".formatted(args.groupId()));
    setModelPackage("%s.model".formatted(args.groupId()));
    setInvokerPackage("%s.invoker".formatted(args.groupId()));
    setArtifactId(title);
    setArtifactVersion(version);
    setDisallowAdditionalPropertiesIfNotPresent(false);
    setLibrary("apache-httpclient");
    setUseBeanValidation(true);
    setUseOneOfInterfaces(false); // TODO delete this
    additionalProperties.put("useOneOfInterfaces", false); // TODO delete this
    setUseOneOfDiscriminatorLookup(true);
    setTemplateDir("templates");

    // TODO need to fix the scm output
    // TODO need to fix the license output
    // TODO need to fix the dev output
    // TODO need to not be fat jar
    // TODO needs to be unsigned
  }

  // TODO document and fix
  private static CodegenModel reconcileInlineEnums(
      CodegenModel codegenModel, CodegenModel parentCodegenModel) {
    // This generator uses inline classes to define enums, which breaks when
    // dealing with models that have subTypes. To clean this up, we will analyze
    // the parent and child models, look for enums that match, and remove
    // them from the child models and leave them in the parent.
    // Because the child models extend the parents, the enums will be available via the parent.

    // Only bother with reconciliation if the parent model has enums.
    if (!parentCodegenModel.hasEnums) {
      return codegenModel;
    }

    // Get the properties for the parent and child models
    final List<CodegenProperty> parentModelCodegenProperties = parentCodegenModel.vars;
    List<CodegenProperty> codegenProperties = codegenModel.vars;

    // Iterate over all of the parent model properties
    boolean removedChildEnum = false;
    for (CodegenProperty parentModelCodegenProperty : parentModelCodegenProperties) {
      // Look for enums
      if (parentModelCodegenProperty.isEnum) {
        // Now that we have found an enum in the parent class,
        // and search the child class for the same enum.
        Iterator<CodegenProperty> iterator = codegenProperties.iterator();
        while (iterator.hasNext()) {
          CodegenProperty codegenProperty = iterator.next();
          if (codegenProperty.isEnum
              && codegenProperty.baseName.equals(parentModelCodegenProperty.baseName)) {
            // We found an enum in the child class that is
            // a duplicate of the one in the parent, so remove it.
            iterator.remove();
            removedChildEnum = true;
          }
        }
      }
    }

    if (removedChildEnum) {
      codegenModel.vars = codegenProperties;
    }
    return codegenModel;
  }

  // TODO cleanup or delete
  @Override
  public Map<String, ModelsMap> postProcessAllModels(final Map<String, ModelsMap> objs) {
    objs.keySet().stream()
        .map(key -> ModelUtils.getModelByName(key, objs))
        .filter(
            model -> model.discriminator != null && model.discriminator.getMappedModels() != null)
        .forEach(
            model -> {
              final Set<CodegenDiscriminator.MappedModel> mappedModels =
                  model.discriminator.getMappedModels().stream()
                      .filter(this::isExplicitMapping)
                      .map(
                          mappedModel -> {
                            final CodegenModel codegenMappedModel =
                                ModelUtils.getModelByName(mappedModel.getModelName(), objs);
                            codegenMappedModel.setParent(model.classname);
                            reconcileInlineEnums(codegenMappedModel, model);
                            return mappedModel;
                          })
                      .collect(Collectors.toSet());
              model.discriminator.setMappedModels(mappedModels);
            });

    return super.postProcessAllModels(objs);
  }

  private boolean isExplicitMapping(@NonNull final CodegenDiscriminator.MappedModel mappedModel) {
    try {
      final Field field =
          CodegenDiscriminator.MappedModel.class.getDeclaredField("explicitMapping");
      field.setAccessible(true);
      return (boolean) field.get(mappedModel);
    } catch (final ReflectiveOperationException ex) {
      throw new RuntimeException("Failed to check for an explicit discriminator mapping", ex);
    }
  }

  // TODO need tests
  private String getOpenapiTitle() {
    return Optional.ofNullable(openAPI.getInfo())
        .map(Info::getTitle)
        .map(title -> title.replaceAll("\\s+", "-"))
        .orElse("unknown-api");
  }

  // TODO need tests
  private String getOpenapiVersion() {
    return Optional.ofNullable(openAPI.getInfo()).map(Info::getVersion).orElse("unknown-version");
  }
}

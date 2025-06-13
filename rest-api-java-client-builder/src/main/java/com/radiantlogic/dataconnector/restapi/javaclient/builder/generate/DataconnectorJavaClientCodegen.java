package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate;

import com.radiantlogic.dataconnector.restapi.javaclient.builder.args.Args;
import com.radiantlogic.dataconnector.restapi.javaclient.builder.io.CodegenPaths;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;
import org.apache.commons.io.FileUtils;
import org.openapitools.codegen.CodegenDiscriminator;
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
  private final Map<String, CodegenModel> modelsByClassName = new HashMap<>();
  private final Map<String, Schema> schemasByClassName = new HashMap<>();

  public DataconnectorJavaClientCodegen(@NonNull final OpenAPI openAPI, @NonNull final Args args) {
    setOpenAPI(openAPI);
    init(args);
  }

  private void init(@NonNull final Args args) {
    final String title = getOpenapiTitle();
    final String version = getOpenapiVersion();
    final Path outputDir = CodegenPaths.OUTPUT_DIR.resolve(title).resolve(version);
    if (Files.exists(outputDir)) { // TODO move to post-init
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
    setUseOneOfInterfaces(true);
    additionalProperties.put("useOneOfInterfaces", true);
    setUseOneOfDiscriminatorLookup(true);
    setLegacyDiscriminatorBehavior(true);
    setUseEnumCaseInsensitive(false);

    setTemplateDir("templates");
    setLibrary("resttemplate");

    // TODO need to fix the scm output
    // TODO need to fix the license output
    // TODO need to fix the dev output
    // TODO need to not be fat jar
    // TODO needs to be unsigned
  }

  // TODO this is to prevent an NPE
  @Override
  public void postProcessModelProperty(final CodegenModel model, final CodegenProperty property) {
    super.postProcessModelProperty(model, property);
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

  @Override
  public CodegenModel fromModel(final String name, final Schema model) {
    final CodegenModel result = super.fromModel(name, model);
    if (result.discriminator != null) {
      result.getVars().stream()
          .filter(prop -> prop.getBaseName().equals(result.discriminator.getPropertyBaseName()))
          .findFirst()
          .ifPresent(
              prop -> {
                result.discriminator.setPropertyType(prop.getDatatypeWithEnum());
              });
    }

    // TODO do I need these? unclear
    modelsByClassName.put(result.classname, result);
    schemasByClassName.put(result.classname, model);
    return result;
  }

  @Override
  public CodegenProperty fromProperty(
      final String name,
      final Schema p,
      final boolean required,
      final boolean schemaIsFromAdditionalProperties) {
    return super.fromProperty(name, p, required, schemaIsFromAdditionalProperties);
  }

  @Override
  protected void addVars(
      final CodegenModel m,
      final Map<String, Schema> properties,
      final List<String> required,
      final Map<String, Schema> allProperties,
      final List<String> allRequired) {
    super.addVars(m, properties, required, allProperties, allRequired);
  }

  // TODO cleanup
  @Override
  public Map<String, ModelsMap> postProcessAllModels(final Map<String, ModelsMap> objs) {
    final List<CodegenModel> newOnes = new ArrayList<>();
    objs.keySet().stream()
        .forEach(
            key -> {
              final CodegenModel model = ModelUtils.getModelByName(key, objs);
              if (model.parentModel != null) {
                model.parentModel.vars.stream()
                    .filter(var -> var.isEnum)
                    .forEach(
                        var -> {
                          var.isEnum = false;
                          var.isEnumRef = true;
                          model.vars.stream()
                              .filter(childVar -> childVar.baseName.equals(var.baseName))
                              .findFirst()
                              .ifPresent(
                                  childVar -> {
                                    childVar.isEnum = false;
                                    childVar.isEnumRef = true;
                                  });
                        });
              }

              if (model.discriminator != null && model.discriminator.getMappedModels() != null) {
                model.vars.stream()
                    .filter(var -> var.isEnum)
                    .forEach(
                        var -> {
                          var.isEnum = false;
                          var.isEnumRef = true;
                          model.discriminator.getMappedModels().stream()
                              .forEach(
                                  mappedModel -> {
                                    final CodegenModel childModel =
                                        ModelUtils.getModelByName(mappedModel.getModelName(), objs);
                                    childModel.vars.stream()
                                        .filter(childVar -> childVar.baseName.equals(var.baseName))
                                        .findFirst()
                                        .ifPresent(
                                            childVar -> {
                                              childVar.isEnum = false;
                                              childVar.isEnumRef = true;
                                            });
                                  });

                          // TODO major cleanup needed
                          final CodegenModel enumModel = new CodegenModel();
                          enumModel.name = var.datatypeWithEnum;
                          enumModel.classname = var.datatypeWithEnum;
                          enumModel.isEnum = true;
                          enumModel.allowableValues = var.allowableValues;
                          enumModel.classFilename = var.datatypeWithEnum;
                          enumModel.dataType = "String";
                          newOnes.add(enumModel);
                        });
              }
            });

    // Name -> models -> importPath/model -> CodegenModel
    // The root map can be cloned from any other.

    final String key = objs.keySet().stream().findFirst().orElseThrow();
    newOnes.forEach(
        model -> {
          final ModelsMap modelsMap = new ModelsMap();
          modelsMap.putAll(objs.get(key));

          final String importPath = toModelImport(model.classname);
          final ModelMap modelMap = new ModelMap();
          modelMap.setModel(model);
          modelMap.put("importPath", importPath);
          modelsMap.setModels(List.of(modelMap));

          objs.put(model.classname, modelsMap);
        });

    return super.postProcessAllModels(objs);
  }

  // TODO cleanup
  private void reconcileEnumsAllParents(
      final CodegenModel model, final String parentName, final Map<String, ModelsMap> objs) {
    if (parentName == null) {
      return;
    }

    final CodegenModel parent = ModelUtils.getModelByName(parentName, objs);
    if (parent == null) {
      return;
    }
    reconcileInlineEnums(model, parent);
    final String grandparentName = parent.getParent();
    reconcileEnumsAllParents(model, grandparentName, objs);
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
}

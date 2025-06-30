package com.radiantlogic.openapi.codegen.javaclient.generate.models;

import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;
import org.openapitools.codegen.CodegenDiscriminator;

public class ExtendedCodegenModelTest {
  static Stream<Arguments> unionTypeArgs() {
    final ExtendedCodegenModel unionWithMapping = new ExtendedCodegenModel();
    unionWithMapping.oneOf = Set.of("one", "two");
    unionWithMapping.discriminator = new CodegenDiscriminator();
    unionWithMapping.discriminator.setPropertyName("type");
    unionWithMapping.discriminator.setMappedModels(
        Set.of(new CodegenDiscriminator.MappedModel("one", "OneModel", true)));

    final ExtendedCodegenModel unionNoMapping = new ExtendedCodegenModel();
    unionNoMapping.oneOf = Set.of("one", "two");
    unionNoMapping.discriminator = new CodegenDiscriminator();
    unionNoMapping.discriminator.setPropertyName("type");

    final ExtendedCodegenModel unionNoDiscriminator = new ExtendedCodegenModel();
    unionNoDiscriminator.oneOf = Set.of("one", "two");

    final ExtendedCodegenModel noUnion = new ExtendedCodegenModel();

    return Stream.of(
        Arguments.of(UnionType.DISCRIMINATED_UNION_WITH_MAPPING, unionWithMapping),
        Arguments.of(UnionType.DISCRIMINATED_UNION_NO_MAPPING, unionNoMapping),
        Arguments.of(UnionType.UNION_NO_DISCRIMINATOR, unionNoDiscriminator),
        Arguments.of(UnionType.NO_UNION, noUnion));
  }
}

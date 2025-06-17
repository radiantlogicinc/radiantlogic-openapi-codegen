package com.radiantlogic.dataconnector.client.usage;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.radiantlogic.custom.dataconnector.radiantonev8api.model.CustomDataSource;
import com.radiantlogic.custom.dataconnector.radiantonev8api.model.DataSourceCategoryEnum;
import com.radiantlogic.custom.dataconnector.radiantonev8api.model.DatabaseDataSource;
import com.radiantlogic.custom.dataconnector.radiantonev8api.model.DirectoryNamespaceEntryScope;
import com.radiantlogic.custom.dataconnector.radiantonev8api.model.ExtensibleObject;
import com.radiantlogic.custom.dataconnector.radiantonev8api.model.ExternalDataSourceInputSource;
import com.radiantlogic.custom.dataconnector.radiantonev8api.model.GenericDataSource;
import com.radiantlogic.custom.dataconnector.radiantonev8api.model.InputSource;
import com.radiantlogic.custom.dataconnector.radiantonev8api.model.LdapDataSource;
import com.radiantlogic.custom.dataconnector.radiantonev8api.model.NamespaceObjectInputSource;
import com.radiantlogic.custom.dataconnector.radiantonev8api.model.PrimaryInputSource;
import com.radiantlogic.custom.dataconnector.radiantonev8api.model.RelatedObject;
import com.radiantlogic.custom.dataconnector.radiantonev8api.model.RequiredDataSourceCategory;
import com.radiantlogic.custom.dataconnector.radiantonev8api.model.SourceTypeEnum;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.lang.NonNull;

/** Test serialization and deserialization of classes that are discriminated unions. */
public class DiscriminatedUnionSerdeTest {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  static Stream<Arguments> radiantoneDatasources() {
    final LdapDataSource ldapDataSource = new LdapDataSource();
    ldapDataSource.setName("myldap");
    ldapDataSource.setType("Active Directory");
    ldapDataSource.setCategory(RequiredDataSourceCategory.LDAP);
    ldapDataSource.setHost("localhost");
    ldapDataSource.setPort(389);
    ldapDataSource.setActive(true);
    ldapDataSource.bindDn("cn=user");
    ldapDataSource.setPassword("password");

    final DatabaseDataSource databaseDataSource = new DatabaseDataSource();
    databaseDataSource.setName("mydb");
    databaseDataSource.setType("MySQL");
    databaseDataSource.setCategory(RequiredDataSourceCategory.DATABASE);
    databaseDataSource.setUrl(URI.create("jdbc:mysql://localhost:3306/mydb"));
    databaseDataSource.setUsername("user");
    databaseDataSource.setPassword("password");
    databaseDataSource.setActive(true);
    databaseDataSource.setDriverClassName("com.mysql.jdbc.Driver");

    final CustomDataSource customDataSource = new CustomDataSource();
    customDataSource.setName("mycustom");
    customDataSource.setType("MyCustomDataSource");
    customDataSource.setCategory(RequiredDataSourceCategory.CUSTOM);
    customDataSource.setActive(true);
    final Map<String, String> props = new HashMap<>();
    props.put("foo", "bar");
    props.put("baz", "qux");
    customDataSource.setCustomProps(props);

    final String ldapJson = readResource("data/discriminatedunionserde/ldap-ds.json");
    final String dbJson = readResource("data/discriminatedunionserde/database-ds.json");
    final String customJson = readResource("data/discriminatedunionserde/custom-ds.json");

    return Stream.of(
        Arguments.of("ldap", ldapDataSource, ldapJson),
        Arguments.of("database", databaseDataSource, dbJson),
        Arguments.of("custom", customDataSource, customJson));
  }

  static Stream<Arguments> radiantoneInputSources() {
    final ExtensibleObject extensibleObject = new ExtensibleObject();
    extensibleObject.setSourceType(SourceTypeEnum.EXTENSIBLE_OBJECT);
    extensibleObject.setName("myextobject");
    extensibleObject.setDataSource("myds");

    final ExternalDataSourceInputSource externalDataSourceInputSource =
        new ExternalDataSourceInputSource();
    externalDataSourceInputSource.setSourceType(SourceTypeEnum.EXTERNAL_DATA_SOURCE);
    externalDataSourceInputSource.setName("myexternal");
    externalDataSourceInputSource.setDataSource("myds");
    externalDataSourceInputSource.setDataSourceCategory(DataSourceCategoryEnum.CUSTOM);
    externalDataSourceInputSource.setSchema("myschema");

    final NamespaceObjectInputSource namespaceObjectInputSource = new NamespaceObjectInputSource();
    namespaceObjectInputSource.setSourceType(SourceTypeEnum.NAMESPACE_OBJECT);
    namespaceObjectInputSource.setName("myns");
    namespaceObjectInputSource.setDataSource("myds");
    namespaceObjectInputSource.setTargetBaseDn("cn=hello");
    namespaceObjectInputSource.setScope(DirectoryNamespaceEntryScope.ONE);

    final PrimaryInputSource primaryInputSource = new PrimaryInputSource();
    primaryInputSource.setSourceType(SourceTypeEnum.PRIMARY);
    primaryInputSource.setName("myprimary");
    primaryInputSource.setDataSource("myds");
    final RelatedObject relatedObject = new RelatedObject();
    relatedObject.setName("myrelated");
    primaryInputSource.addRelatedObjectsItem(relatedObject);

    final String extensibleJson = readResource("data/discriminatedunionserde/extensible-is.json");
    final String externalJson = readResource("data/discriminatedunionserde/external-is.json");
    final String namespaceJson = readResource("data/discriminatedunionserde/namespace-is.json");
    final String primaryJson = readResource("data/discriminatedunionserde/primary-is.json");

    return Stream.of(
        Arguments.of("extensible", extensibleObject, extensibleJson),
        Arguments.of("external", externalDataSourceInputSource, externalJson),
        Arguments.of("namespace", namespaceObjectInputSource, namespaceJson),
        Arguments.of("primary", primaryInputSource, primaryJson));
  }

  @SneakyThrows
  private static String readResource(@NonNull final String resourceName) {
    final InputStream stream =
        DiscriminatedUnionSerdeTest.class.getClassLoader().getResourceAsStream(resourceName);
    if (stream == null) {
      throw new IllegalArgumentException("Resource not found: " + resourceName);
    }

    try {
      return IOUtils.toString(stream, StandardCharsets.UTF_8);
    } finally {
      stream.close();
    }
  }

  @ParameterizedTest(name = "It handles radiantone datasources: {0}")
  @MethodSource("radiantoneDatasources")
  @SneakyThrows
  void itHandlesRadiantoneDatasources(
      @NonNull final String name,
      @NonNull final GenericDataSource dataSource,
      @NonNull final String json) {
    final String actualJson = objectMapper.writeValueAsString(dataSource);

    assertThatJson(actualJson).isEqualTo(json);

    final GenericDataSource actualDataSource =
        objectMapper.readValue(actualJson, GenericDataSource.class);
    assertThat(actualDataSource).usingRecursiveComparison().isEqualTo(dataSource);
  }

  @ParameterizedTest(name = "It handles radiantone input sources: {0}")
  @MethodSource("radiantoneInputSources")
  @SneakyThrows
  void itHandlesRadiantoneInputSources(
      @NonNull final String name,
      @NonNull final InputSource inputSource,
      @NonNull final String json) {
    final String actualJson = objectMapper.writeValueAsString(inputSource);

    assertThatJson(actualJson).isEqualTo(json);

    final InputSource actualInputSource = objectMapper.readValue(actualJson, InputSource.class);
    assertThat(actualInputSource).usingRecursiveComparison().isEqualTo(inputSource);
  }
}

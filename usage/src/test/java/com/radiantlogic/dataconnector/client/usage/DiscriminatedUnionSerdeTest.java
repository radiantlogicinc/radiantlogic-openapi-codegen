package com.radiantlogic.dataconnector.client.usage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.radiantlogic.custom.dataconnector.radiantonev8api.model.CustomDataSource;
import com.radiantlogic.custom.dataconnector.radiantonev8api.model.DatabaseDataSource;
import com.radiantlogic.custom.dataconnector.radiantonev8api.model.GenericDataSource;
import com.radiantlogic.custom.dataconnector.radiantonev8api.model.LdapDataSource;
import com.radiantlogic.custom.dataconnector.radiantonev8api.model.RequiredDataSourceCategory;
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

    final CustomDataSource customDataSource = new CustomDataSource();
    customDataSource.setName("mycustom");
    customDataSource.setType("MyCustomDataSource");
    customDataSource.setCategory(RequiredDataSourceCategory.CUSTOM);
    customDataSource.setActive(true);
    final Map<String, String> props = new HashMap<>();
    props.put("foo", "bar");
    props.put("baz", "qux");
    customDataSource.setCustomProps(props);

    return Stream.of(
        Arguments.of("ldap", ldapDataSource, ""),
        Arguments.of("database", databaseDataSource, ""),
        Arguments.of("custom", customDataSource, ""));
  }

  @SneakyThrows
  private static String readResource(@NonNull final String resourceName) {
    final InputStream stream = DiscriminatedUnionSerdeTest.class.getResourceAsStream(resourceName);
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
      @NonNull final String name, @NonNull final GenericDataSource dataSource) {
    final String json = objectMapper.writeValueAsString(dataSource);
    System.out.println(json);
  }
}

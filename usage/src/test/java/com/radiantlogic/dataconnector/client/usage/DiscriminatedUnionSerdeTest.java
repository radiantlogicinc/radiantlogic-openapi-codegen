package com.radiantlogic.dataconnector.client.usage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.radiantlogic.custom.dataconnector.radiantonev8api.model.LdapDataSource;
import com.radiantlogic.custom.dataconnector.radiantonev8api.model.RequiredDataSourceCategory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

/**
 * Test serialization and deserialization of classes that are discriminated unions.
 */
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



        return Stream.of(
            Arguments.of("ldap", ldapDataSource),
                Arguments.of("database"),
                Arguments.of("custom")
        );
    }

    @ParameterizedTest(name = "It handles radiantone datasources: {0}")
    @MethodSource("radiantoneDatasources")
    void itHandlesRadiantoneDatasources() {

    }
}

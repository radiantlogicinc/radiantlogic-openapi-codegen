package com.radiantlogic.dataconnector.usage.radiantone;

import com.radiantlogic.custom.dataconnector.radiantonev8api.api.DataSourcesApi;
import com.radiantlogic.custom.dataconnector.radiantonev8api.invoker.ApiClient;
import com.radiantlogic.custom.dataconnector.radiantonev8api.model.GenericDataSource;
import com.radiantlogic.custom.dataconnector.radiantonev8api.model.LdapDataSource;

public class DatasourceExample {
    public static void main(final String[] args) throws Exception {
        final String basePath = "";
        final String username = "";
        final String password = "";

        final ApiClient apiClient = new ApiClientProvider().createAndPrepareApiClient(basePath, username, password);

        final DataSourcesApi dataSourcesApi = new DataSourcesApi(apiClient);

        final LdapDataSource ldapDataSource = new LdapDataSource();
        ldapDataSource.setHost("localhost");
        ldapDataSource.setPort(389);
        ldapDataSource.setCategory(com.radiantlogic.custom.dataconnector.radiantonev8api.model.RequiredDataSourceCategory.LDAP);
        ldapDataSource.setType("Active Directory");
        ldapDataSource.setName("mydatasource");

        dataSourcesApi.createDataSource(ldapDataSource);
        final LdapDataSource result = (LdapDataSource) dataSourcesApi.getDataSource(ldapDataSource.getName());
    }
}

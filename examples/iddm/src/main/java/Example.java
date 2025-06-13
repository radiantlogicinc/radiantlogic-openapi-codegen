import com.radiantlogic.custom.dataconnector.api.AuthTokenApiApi;
import com.radiantlogic.custom.dataconnector.api.DataSourcesApi;
import com.radiantlogic.custom.dataconnector.invoker.ApiClient;
import com.radiantlogic.custom.dataconnector.model.LdapDataSource;
import java.util.Base64;

public class Example {
  public static void main(final String[] args) throws Exception {
    final ApiClient apiClient = new ApiClient();
    apiClient.setBasePath("https://rlqa-usw2-craig.dev01.radiantlogic.io/api");

    final AuthTokenApiApi authTokenApiApi = new AuthTokenApiApi(apiClient);
    final String basicAuth = "%s:%s".formatted("username", "password");
    final String base64EncodedAuth = Base64.getEncoder().encodeToString(basicAuth.getBytes());
    final String token =
        authTokenApiApi.postLogin("Basic %s".formatted(base64EncodedAuth)).getToken();

    apiClient.setBearerToken(token);

    final DataSourcesApi dataSourcesApi = new DataSourcesApi(apiClient);
    final LdapDataSource ldapDataSource = new LdapDataSource();
    ldapDataSource.setHost("localhost");
    ldapDataSource.setPort(389);
    dataSourcesApi.createDataSource(ldapDataSource);
  }
}

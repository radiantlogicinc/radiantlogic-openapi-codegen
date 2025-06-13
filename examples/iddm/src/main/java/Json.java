import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.radiantlogic.custom.dataconnector.model.GenericDataSource;
import com.radiantlogic.custom.dataconnector.model.RequiredDataSourceCategory;

public class Json {
  public static void main(String[] args) throws Exception {
    final ObjectMapper objectMapper = new ObjectMapper();
    System.out.println(objectMapper.writeValueAsString(RequiredDataSourceCategory.LDAP));

    final LdapDataSource ldapHolder = new LdapDataSource();
    ldapHolder.setCategory(RequiredDataSourceCategory.LDAP);
    System.out.println(objectMapper.writeValueAsString(ldapHolder));
  }

  @JsonTypeName("ldap")
  public static class LdapDataSource implements GenericDataSource {
    public static final String JSON_PROPERTY_CATEGORY = "category";
    @javax.annotation.Nonnull private RequiredDataSourceCategory category;

    @Override
    public RequiredDataSourceCategory getCategory() {
      return category;
    }

    public void setCategory(final RequiredDataSourceCategory category) {
      this.category = category;
    }
  }
}

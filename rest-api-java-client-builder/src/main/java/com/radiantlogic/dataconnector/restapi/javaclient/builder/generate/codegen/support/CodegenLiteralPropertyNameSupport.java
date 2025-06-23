package com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.codegen.support;

import com.radiantlogic.dataconnector.restapi.javaclient.builder.generate.models.ExtendedCodegenProperty;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

/** Valid JSON property names are not always valid Java field names. This corrects any problems. */
public class CodegenLiteralPropertyNameSupport {
  public void fixBadNames(@NonNull final ExtendedCodegenProperty prop) {
    if (prop.name != null && StringUtils.isNumeric(prop.name)
        || "true".equals(prop.name)
        || "false".equals(prop.name)) {
      prop.jsonName = prop.name;
      prop.name =
          "value%s%s"
              .formatted(String.valueOf(prop.name.charAt(0)).toUpperCase(), prop.name.substring(1));
    }
  }
}

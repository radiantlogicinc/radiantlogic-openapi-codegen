package com.radiantlogic.openapi.builder.javaclient.generate.codegen.support;

import com.radiantlogic.openapi.builder.javaclient.generate.models.ExtendedCodegenProperty;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

/** Valid JSON property names are not always valid Java field names. This corrects any problems. */
public class CodegenLiteralPropertyNameSupport {
  public void fixBadNames(@NonNull final ExtendedCodegenProperty prop) {
    if (prop.name == null) {
      return;
    }

    if (StringUtils.isNumeric(prop.name) || "true".equals(prop.name) || "false".equals(prop.name)) {
      prop.jsonName = prop.name;
      prop.name =
          "value%s%s"
              .formatted(String.valueOf(prop.name.charAt(0)).toUpperCase(), prop.name.substring(1));
    }
  }
}

package com.radiantlogic.dataconnector.codegen.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropsReader {
  public Props readProps() throws IOException {
    try (InputStream stream =
        getClass().getClassLoader().getResourceAsStream("project.properties")) {
      final Properties props = new Properties();
      props.load(stream);
      return new Props(
          props.getProperty("project.artifactId"), props.getProperty("project.version"));
    }
  }
}

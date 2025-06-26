package com.radiantlogic.openapi.codegen.javaclient.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The project properties are created by the maven-resources-plugin and expose project metadata to
 * the application at runtime. This class parses that file and returns it as a record.
 */
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

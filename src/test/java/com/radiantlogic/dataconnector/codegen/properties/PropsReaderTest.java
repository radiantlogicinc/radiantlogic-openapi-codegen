package com.radiantlogic.dataconnector.codegen.properties;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PropsReaderTest {
  private static String artifactId;
  private static String version;

  @BeforeAll
  @SneakyThrows
  static void beforeAll() {
    final Path pomXmlPath = Path.of(System.getProperty("user.dir"), "pom.xml");
    final Document doc;
    try (InputStream stream = Files.newInputStream(pomXmlPath)) {
      doc = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder().parse(stream);
    }
    final Element root = doc.getDocumentElement();
    final Element project = getSingleElement(root, "project");
    artifactId = getSingleElement(project, "artifactId").getTextContent();
    version = getSingleElement(project, "version").getTextContent();
  }

  private static Element getSingleElement(final Element base, final String tagName) {
    return (Element) base.getElementsByTagName(tagName).item(0);
  }

  @Test
  void itReadsPropsSetByMavenPlugin() {
    throw new RuntimeException();
  }
}

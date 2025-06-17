package com.radiantlogic.dataconnector.restapi.javaclient.builder.properties;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PropsReaderTest {
  private static String artifactId;
  private static String version;

  private final PropsReader propsReader = new PropsReader();

  @BeforeAll
  @SneakyThrows
  static void beforeAll() {
    final Path pomXmlPath = Path.of(System.getProperty("user.dir"), "pom.xml");
    final Path parentPomXmlPath = Path.of(System.getProperty("user.dir"), "..", "pom.xml");

    final DocumentBuilder docBuilder =
        DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();

    final Document pomDoc;
    try (InputStream stream = Files.newInputStream(pomXmlPath)) {
      pomDoc = docBuilder.parse(stream);
    }
    artifactId = getSingleElement(pomDoc.getDocumentElement(), "artifactId").getTextContent();

    final Document parentPomDoc;
    try (InputStream stream = Files.newInputStream(parentPomXmlPath)) {
      parentPomDoc = docBuilder.parse(stream);
    }

    final Element propsElement = getSingleElement(parentPomDoc.getDocumentElement(), "properties");
    version = getSingleElement(propsElement, "revision").getTextContent();
  }

  private static Element getSingleElement(
      @NonNull final Element base, @NonNull final String tagName) {
    return (Element) base.getElementsByTagName(tagName).item(0);
  }

  /**
   * If run via IntelliJ, this will not work unless you do the following:
   *
   * <p>1. Open the run configuration for the test.
   *
   * <p>2. Select Modify Options -> Before Launch Task
   *
   * <p>3. Add a new task, "Add Maven Goal"
   *
   * <p>4. The "command line options" should be "generate-resources"
   *
   * <p>This is because IntelliJ by default doesn't run the maven lifecycle, it runs its own system.
   * This test depends on a properties file generated at compile time with the
   * maven-resources-plugin, which is necessary.
   */
  @Test
  @SneakyThrows
  void itReadsPropsSetByMavenPlugin() {
    final Props props = propsReader.readProps();
    final Props expectedProps = new Props(artifactId, version);
    assertThat(props).isEqualTo(expectedProps);
  }
}

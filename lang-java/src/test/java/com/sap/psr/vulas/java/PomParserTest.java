package com.sap.psr.vulas.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class PomParserTest {

  @Test
  public void testParsePom() {
    try {
      final PomParser pp = new PomParser();
      final SAXParserFactory spf = SAXParserFactory.newInstance();
      spf.setNamespaceAware(true);
      final SAXParser saxParser = spf.newSAXParser();
      final XMLReader xmlReader = saxParser.getXMLReader();
      xmlReader.setContentHandler(pp);
      xmlReader.parse(new InputSource(new FileReader(new File("pom.xml"))));
      assertEquals("com.sap.research.security.vulas", pp.getLibraryId().getMvnGroup());
      assertEquals("lang-java", pp.getLibraryId().getArtifact());
    } catch (Exception e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }
}

package com.sap.psr.vulas.java;

import com.sap.psr.vulas.shared.json.model.LibraryId;
import java.util.Arrays;
import java.util.Stack;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** Creates a {@link LibraryId} for a given pom.xml. */
public class PomParser extends DefaultHandler {

  private static final Log log = LogFactory.getLog(PomParser.class);

  private static final String[] pg = new String[] {"project", "parent", "groupId"};
  private static final String[] pa = new String[] {"project", "parent", "artifactId"};
  private static final String[] pv = new String[] {"project", "parent", "version"};

  private static final String[] g = new String[] {"project", "groupId"};
  private static final String[] a = new String[] {"project", "artifactId"};
  private static final String[] v = new String[] {"project", "version"};

  private Stack<String> stack = new Stack<String>();

  private LibraryId libid = new LibraryId();

  /** {@inheritDoc} */
  public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
      throws SAXException {
    this.stack.push(localName);
  }

  /** {@inheritDoc} */
  public void characters(char ch[], int start, int length) throws SAXException {
    // log.info(stack.toString());
    if (Arrays.equals(stack.toArray(), pg) || Arrays.equals(stack.toArray(), g)) {
      if (libid.getMvnGroup() == null || Arrays.equals(stack.toArray(), g))
        libid.setMvnGroup(new String(Arrays.copyOfRange(ch, start, start + length)));
    } else if (Arrays.equals(stack.toArray(), pa) || Arrays.equals(stack.toArray(), a)) {
      if (libid.getArtifact() == null || Arrays.equals(stack.toArray(), a))
        libid.setArtifact(new String(Arrays.copyOfRange(ch, start, start + length)));
    } else if (Arrays.equals(stack.toArray(), pv) || Arrays.equals(stack.toArray(), v)) {
      if (libid.getVersion() == null || Arrays.equals(stack.toArray(), v))
        libid.setVersion(new String(Arrays.copyOfRange(ch, start, start + length)));
    }
  }

  /** {@inheritDoc} */
  public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
    this.stack.pop();
  }

  /**
   * getLibraryId.
   *
   * @return a {@link com.sap.psr.vulas.shared.json.model.LibraryId} object.
   */
  public LibraryId getLibraryId() {
    return this.libid;
  }
}

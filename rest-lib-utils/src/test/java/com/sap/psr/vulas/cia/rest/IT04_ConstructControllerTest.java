package com.sap.psr.vulas.cia.rest;

import static org.junit.Assert.assertTrue;

import com.sap.psr.vulas.FileAnalysisException;
import com.sap.psr.vulas.cia.util.ClassDownloader;
import com.sap.psr.vulas.java.JavaFileAnalyzer2;
import com.sap.psr.vulas.java.JavaId;
import java.nio.file.Path;
import org.junit.Test;

public class IT04_ConstructControllerTest {

  @Test
  public void getConstructforGavTest() {

    JavaId jid =
        JavaId.parseMethodQName(
            "org.apache.cxf.jaxrs.provider.atom.AbstractAtomProvider.readFrom(Class,Type,Annotation[],MediaType,MultivaluedMap,InputStream)");
    JavaId ctx = (JavaId) jid.getDefinitionContext();
    Path file =
        ClassDownloader.getInstance()
            .getClass(
                "org.apache.cxf",
                "cxf-rt-rs-extension-providers",
                "2.6.2-sap-02",
                ctx.getQualifiedName(),
                ClassDownloader.Format.JAVA);

    if (file == null) {
      System.out.println("Cannot retrieve class");

    } else {
      // Use ANTLR to parse the Java file
      JavaFileAnalyzer2 jfa = new JavaFileAnalyzer2();
      try {
        jfa.analyze(file.toFile());
        assertTrue(jfa.containsConstruct(jid));
      } catch (FileAnalysisException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
}

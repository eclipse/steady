package com.sap.psr.vulas.java.decompiler;

import static org.junit.Assert.assertTrue;

import com.sap.psr.vulas.Construct;
import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.FileAnalysisException;
import com.sap.psr.vulas.FileAnalyzer;
import com.sap.psr.vulas.FileAnalyzerFactory;
import com.sap.psr.vulas.java.JavaClassId;
import com.sap.psr.vulas.java.JavaId;
import java.io.File;
import java.util.Map;
import org.junit.Test;

public class IDecompilerTest {

  /** Test class to be decompiled. */
  class NonStaticInner {
    NonStaticInner() {}

    void foo() {}
  }

  /** Test class to be decompiled. */
  static class StaticInner {
    StaticInner() {}

    void foo() {}
  }

  /**
   * Test whether the decompiler properly constructs the names of inner classes (in the form
   * Outer$Inner). As explained in the Procyon ticket #283
   * (https://bitbucket.org/mstrobel/procyon/issues/283), this does not work for an inner class from
   * Apache FileUpload.
   */
  @Test
  public void testDecompileAnonClass() {
    try {
      // Decompile and get constructs
      final IDecompiler decompiler = new ProcyonDecompiler();
      final File java_source_file =
          decompiler.decompileClassFile(
              new File(
                  "./target/test-classes/com/sap/psr/vulas/java/decompiler/IDecompilerTest$NonStaticInner.class"));
      final FileAnalyzer jfa = FileAnalyzerFactory.buildFileAnalyzer(java_source_file);
      final Map<ConstructId, Construct> constructs = jfa.getConstructs();

      // Expected construct
      JavaClassId inner_class =
          JavaId.parseClassQName(
              "com.sap.psr.vulas.sign.decompiler.IDecompilerTest$NonStaticInner");

      // TODO: Change as soon (if ever) this is fixed
      assertTrue(!constructs.containsKey(inner_class));
    } catch (FileAnalysisException e) {
    }
  }
}

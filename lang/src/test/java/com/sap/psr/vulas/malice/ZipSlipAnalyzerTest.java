package com.sap.psr.vulas.malice;

import static org.junit.Assert.assertEquals;

import java.io.File;
import org.junit.Test;

public class ZipSlipAnalyzerTest {

  @Test
  public void testZipSlipFile() {
    final MaliciousnessAnalyzer mala = new ZipSlipAnalyzer();

    MaliciousnessAnalysisResult is_mal;

    // Test the 4 archives from https://github.com/snyk/zip-slip-vulnerability

    is_mal = mala.isMalicious(new File("src/test/resources/zip-slip.zip"));
    assertEquals(1d, is_mal.getResult(), 0d);

    is_mal = mala.isMalicious(new File("src/test/resources/zip-slip-win.zip"));
    assertEquals(1d, is_mal.getResult(), 0d);

    is_mal = mala.isMalicious(new File("src/test/resources/zip-slip.tar"));
    assertEquals(1d, is_mal.getResult(), 0d);

    is_mal = mala.isMalicious(new File("src/test/resources/zip-slip-win.tar"));
    assertEquals(1d, is_mal.getResult(), 0d);

    // A malicious JAR
    is_mal = mala.isMalicious(new File("src/test/resources/zip-slip.jar"));
    assertEquals(1d, is_mal.getResult(), 0d);

    // A benign ZIP
    is_mal = mala.isMalicious(new File("src/test/resources/no-zip-slip.zip"));
    assertEquals(0d, is_mal.getResult(), 0d);
  }
}

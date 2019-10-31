package com.sap.psr.vulas.malice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Set;
import org.junit.Test;

public class MaliciousnessAnalyzerLoopTest {

  @Test
  public void testLoop() {
    final MaliciousnessAnalyzerLoop loop = new MaliciousnessAnalyzerLoop();
    final Set<MaliciousnessAnalysisResult> results =
        loop.isMalicious(new File("src/test/resources/zip-slip.zip"));
    assertTrue(results.size() > 0);
    assertEquals(1d, results.iterator().next().getResult(), 0d);
  }
}

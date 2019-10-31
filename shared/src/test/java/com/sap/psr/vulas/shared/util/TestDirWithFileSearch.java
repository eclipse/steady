package com.sap.psr.vulas.shared.util;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import org.junit.Test;

public class TestDirWithFileSearch {

  @Test
  public void testSearch() {
    final DirWithFileSearch s = new DirWithFileSearch("readme.txt");

    // 1 hit
    Set<Path> r = s.search(Paths.get("src/test/resources/foo"));
    assertEquals(1, r.size());

    // 0 hits
    s.clear();
    r = s.search(Paths.get("src/test/resources/foo/empty"));
    assertEquals(0, r.size());
  }
}

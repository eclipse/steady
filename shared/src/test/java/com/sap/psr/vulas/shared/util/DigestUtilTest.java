package com.sap.psr.vulas.shared.util;

import static org.junit.Assert.assertEquals;

import com.sap.psr.vulas.shared.enums.DigestAlgorithm;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

public class DigestUtilTest {

  @Test
  public void testGetDigestAsBytes() throws IOException {
    final String text = "foo bar baz";
    final String sha1 =
        DigestUtil.getDigestAsString(text, StandardCharsets.UTF_8, DigestAlgorithm.SHA1)
            .toLowerCase();
    final String expected_sha1 = "c7567e8b39e2428e38bf9c9226ac68de4c67dc39";
    assertEquals(expected_sha1, sha1);
  }

  @Test
  public void testGetDigestAsBytesFromFile() throws IOException {
    final String text = FileUtil.readFile("./src/test/resources/foo.txt");
    assertEquals("foo bar baz", text);
    final String sha1 =
        DigestUtil.getDigestAsString(text, StandardCharsets.UTF_8, DigestAlgorithm.SHA1)
            .toLowerCase();
    final String expected_sha1 = "c7567e8b39e2428e38bf9c9226ac68de4c67dc39";
    assertEquals(expected_sha1, sha1);
  }
}

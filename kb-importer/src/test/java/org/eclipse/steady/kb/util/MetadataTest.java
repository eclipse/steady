package org.eclipse.steady.kb.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.io.IOException;

import org.eclipse.steady.kb.model.Commit;
import org.eclipse.steady.kb.model.Vulnerability;
import org.junit.Test;
import com.google.gson.JsonSyntaxException;

public class MetadataTest {
  @Test
  public void testGetVulnMetadata() throws JsonSyntaxException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String path = classLoader.getResource("testRootDir1").getPath();
    Vulnerability vuln = Metadata.getVulnerabilityMetadata(path);
    assertEquals(3, vuln.getArtifacts().size());
    assertEquals(3, vuln.getNotes().size());
    assertEquals("COLLECTIONS-580", vuln.getVulnId());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidVulnRootDir() throws JsonSyntaxException, IOException {
    Metadata.getVulnerabilityMetadata("rootDir1test");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNoVulnIdArg() throws Exception, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String path = classLoader.getResource("testRootDir2").getPath();
    Metadata.getVulnerabilityMetadata(path);
  }

  @Test
  public void testMissingNonMandatoryParams() throws JsonSyntaxException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String path = classLoader.getResource("testRootDir3").getPath();
    Vulnerability vuln = Metadata.getVulnerabilityMetadata(path);
    assertNull(vuln.getNotes());
    assertEquals("COLLECTIONS-580", vuln.getVulnId());
  }

  @Test
  public void testGetCommitMetadata() throws JsonSyntaxException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String path = classLoader.getResource("commitDir1").getPath();
    Commit commit = Metadata.getCommitMetadata(path);
    assertEquals("master", commit.getBranch());
    assertEquals("b2b8f4adc557e4ef1ee2fe5e0ab46866c06ec55b", commit.getCommitId());
    assertEquals("1447974481000", commit.getTimestamp());
    assertEquals("https://github.com/apache/commons-collections", commit.getRepoUrl());
    assertEquals(path, commit.getDirectory());
  }

  @Test
  public void testMetadataArtifacts() throws JsonSyntaxException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String path = classLoader.getResource("testRootDir4").getPath();
    Vulnerability vuln = Metadata.getVulnerabilityMetadata(path);
    assertEquals(3, vuln.getArtifacts().size());
  }

  @Test
  public void testMetadataArtifactsPUrl() throws JsonSyntaxException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String path = classLoader.getResource("testRootDir5").getPath();
    Vulnerability vuln = Metadata.getVulnerabilityMetadata(path);
    assertEquals(1, vuln.getArtifacts().size());
    assertEquals(false, vuln.getArtifacts().get(0).getAffected());
    assertEquals(
        "pkg:maven://org.springframework.security/spring-security-config@5.1.1.RELEASE",
        vuln.getArtifacts().get(0).getId());
    assertEquals("Manual review(frontend)", vuln.getArtifacts().get(0).getReason());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidCommitMetadataDir() throws JsonSyntaxException, IOException {
    Metadata.getVulnerabilityMetadata("commitDir2");
  }
}

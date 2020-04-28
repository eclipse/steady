package com.sap.psr.vulas.kb.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import com.sap.psr.vulas.kb.meta.Commit;
import com.sap.psr.vulas.kb.meta.Vulnerability;

public class MetadataTest {
  @Test
  public void testGetVulnMetadata() {
    ClassLoader classLoader = getClass().getClassLoader();
    String path = classLoader.getResource("testRootDir1").getPath();
    Vulnerability vuln = Metadata.getVulnerabilityMetadata(path);
    assertEquals(
        "Arbitrary remote code execution with InvokerTransformer. With InvokerTransformer serializable collections can be build that execute arbitrary Java code. sun.reflect.annotation.AnnotationInvocationHandler#readObject invokes #entrySet and #get on a deserialized collection. If you have an endpoint that accepts serialized Java objects (JMX, RMI, remote EJB, ...) you can combine the two to create arbitrary remote code execution vulnerability. Fixed in versions 3.2.2, 4.1",
        vuln.getDescription());
    assertEquals(2, vuln.getLinks().size());
    assertEquals("COLLECTIONS-580", vuln.getVulnId());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidVulnRootDir() {
    Metadata.getVulnerabilityMetadata("rootDir1test");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNoVulnIdArg() {
    ClassLoader classLoader = getClass().getClassLoader();
    String path = classLoader.getResource("testRootDir2").getPath();
    Metadata.getVulnerabilityMetadata(path);
  }

  @Test
  public void testMissingNonMandatoryParams() {
    ClassLoader classLoader = getClass().getClassLoader();
    String path = classLoader.getResource("testRootDir3").getPath();
    Vulnerability vuln = Metadata.getVulnerabilityMetadata(path);
    assertNull(vuln.getLinks());
    assertEquals("test", vuln.getVulnId());
  }

  @Test
  public void testGetCommitMetadata() {
    ClassLoader classLoader = getClass().getClassLoader();
    String path = classLoader.getResource("commitDir1").getPath();
    Commit commit = Metadata.getCommitMetadata(path);
    assertEquals("master", commit.getBranch());
    assertEquals("b2b8f4adc557e4ef1ee2fe5e0ab46866c06ec55b", commit.getCommitId());
    assertEquals("1447974481000", commit.getTimestamp());
    assertEquals("https://github.com/apache/commons-collections", commit.getRepoUrl());
    assertEquals(path, commit.getDirectory());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidCommitMetadataDir() {
    Metadata.getVulnerabilityMetadata("commitDir2");
  }
}

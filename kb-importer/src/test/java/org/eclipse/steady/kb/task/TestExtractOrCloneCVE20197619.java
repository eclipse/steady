package org.eclipse.steady.kb.task;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.eclipse.steady.kb.ImportCommand;
import org.eclipse.steady.kb.Manager;
import org.eclipse.steady.kb.model.Vulnerability;
import org.eclipse.steady.kb.util.Metadata;
import org.eclipse.steady.shared.enums.DigestAlgorithm;
import org.eclipse.steady.shared.util.FileUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Tests the {@link ExtractOrClone} task with CVE-2019-7619, whose fix commits
 * added new files. Depending on whether the dir parameter, the repo will be
 * cloned (<p>statements/CVE-2019-7619</p>) or not (n/a).
 */
@RunWith(Parameterized.class)
public class TestExtractOrCloneCVE20197619 {

  @Parameterized.Parameters
  public static List directories() {
    return Arrays.asList(new Object[][] { {"statements/CVE-2019-7619"}});
  }

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();
  
  Manager manager;
  ClassLoader classLoader;
  String dirPath;
  File dir;

  @Before
  public void initialize() {
    this.manager = new Manager();
    classLoader = getClass().getClassLoader();
  }

  public TestExtractOrCloneCVE20197619(String dirName) {
    classLoader = getClass().getClassLoader();
    this.dirPath = classLoader.getResource(dirName).getPath();
    this.dir = new File(dirPath);
  }

  @Test
  public void testClone() throws IOException {

    String statementPath = dirPath + File.separator + "statement.yaml";

    Vulnerability vuln = Metadata.getFromYaml(statementPath);

    HashMap<String, Object> args = new HashMap<String, Object>();
    //args.put("v", false);
    args.put(ImportCommand.OVERWRITE_OPTION, false);
    args.put(ImportCommand.DIRECTORY_OPTION, "");

    ExtractOrClone extractOrClone = new ExtractOrClone(manager, vuln, this.dir, false);
    extractOrClone.execute();

    File commitDir1before =
        new File(
            dirPath
                + File.separator
                + "9964d89dd5d67cf72a85eb48d76347f09bd875f"
                + File.separator
                + "before");
    File commitDir1after =
        new File(
            dirPath
                + File.separator
                + "9964d89dd5d67cf72a85eb48d76347f09bd875f"
                + File.separator
                + "after");

    assertTrue(commitDir1before.exists());
    assertTrue(commitDir1after.exists());
    
    // Two subdirectories each
    assertTrue(commitDir1before.list().length == 2);
    assertTrue(commitDir1after.list().length == 2);
    
    // Ensure files exist in after/ with given SHA1 digests
    Path only_after_file = commitDir1after.toPath().resolve(Paths.get("client/rest-high-level/roles.yml"));
    assertTrue(only_after_file.toFile().exists());
    assertEquals(FileUtil.getDigest(only_after_file.toFile(), DigestAlgorithm.SHA1), "b32c4d48d2197602d0c03b35a29b32d0ec94b1cc".toUpperCase());
    only_after_file = commitDir1after.toPath().resolve(Paths.get("x-pack/plugin/src/test/resources/rest-api-spec/test/api_key/11_invalidation.yml"));
    assertTrue(only_after_file.toFile().exists());
    assertEquals(FileUtil.getDigest(only_after_file.toFile(), DigestAlgorithm.SHA1), "9ea2ba219fd84f6c5cf974ce37b6663cb7961444".toUpperCase());

    // Ensure files do not exist in before/
    Path not_before_file = commitDir1before.toPath().resolve(Paths.get("client/rest-high-level/roles.yml"));
    assertFalse(not_before_file.toFile().exists());
    not_before_file = commitDir1before.toPath().resolve(Paths.get("x-pack/plugin/src/test/resources/rest-api-spec/test/api_key/11_invalidation.yml"));
    assertFalse(not_before_file.toFile().exists());
  }

  @After
  public void cleanup() {
    try {
      FileUtils.deleteDirectory(this.dir);
    } catch (IOException e) {
      log.error(e.getMessage());
    }
  }
}

package org.eclipse.steady.kb.task;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.eclipse.steady.kb.ImportCommand;
import org.eclipse.steady.kb.Manager;
import org.eclipse.steady.kb.model.Vulnerability;
import org.eclipse.steady.kb.util.Metadata;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Tests the {@link ExtractOrClone} task with CVE-2018-1270, whose fix commits
 * only modified existing files. Depending on whether the dir parameter, the
 * repo will be cloned (<p>testRootDir7</p>) or not
 * (<p>statements/CVE-2018-1270</p>).
 */
@RunWith(Parameterized.class)
public class TestExtractOrCloneCVE20181270 {

  @Parameterized.Parameters
  public static List directories() {
    return Arrays.asList(new Object[][] {{"statements/CVE-2018-1270"}, {"testRootDir7"}});
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

  public TestExtractOrCloneCVE20181270(String dirName) {
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
                + "1db7e02de3eb0c011ee6681f5a12eb9d166fea8"
                + File.separator
                + "before");
    File commitDir1after =
        new File(
            dirPath
                + File.separator
                + "1db7e02de3eb0c011ee6681f5a12eb9d166fea8"
                + File.separator
                + "after");
    File someJavaFile =
        new File(
            dirPath
                + File.separator
                + "1db7e02de3eb0c011ee6681f5a12eb9d166fea8/after/spring-expression/src/test/java/org/springframework/expression/spel/SpelCompilationCoverageTests.java");
    File commitDir2 =
        new File(dirPath + File.separator + "d3acf45ea4db51fa5c4cbd0bc0e7b6d9ef805e6");
    File commitDir3 =
        new File(dirPath + File.separator + "e0de9126ed8cf25cf141d3e66420da94e350708");

    org.junit.Assert.assertEquals(commitDir1before.exists(), true);
    org.junit.Assert.assertEquals(commitDir1after.exists(), true);

    org.junit.Assert.assertEquals(commitDir1before.list().length == 1, true);
    org.junit.Assert.assertEquals(commitDir1after.list().length == 1, true);

    org.junit.Assert.assertEquals(someJavaFile.exists(), true);
    org.junit.Assert.assertEquals(commitDir2.exists(), true);
    org.junit.Assert.assertEquals(commitDir3.exists(), true);
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

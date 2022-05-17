package org.eclipse.steady.kb.task;

import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;

import org.eclipse.steady.kb.model.Vulnerability;
import org.eclipse.steady.kb.Import;
import org.eclipse.steady.kb.Manager;
import org.eclipse.steady.kb.util.Metadata;
import org.eclipse.steady.shared.util.VulasConfiguration;
import com.github.packageurl.MalformedPackageURLException;
import com.google.gson.JsonSyntaxException;

import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runner.RunWith;

import org.apache.commons.io.FileUtils;

@RunWith(Parameterized.class)
public class TestExtractOrClone {

  Manager manager;
  ClassLoader classLoader;
  String dirPath;
  File dir;

  @Before
  public void initialize() {
    this.manager = new Manager();
    classLoader = getClass().getClassLoader();
    //this.dirPath = classLoader.getResource("testRootDir7").getPath();
  }

  public TestExtractOrClone(String dirName) {
    classLoader = getClass().getClassLoader();
    this.dirPath = classLoader.getResource(dirName).getPath();
    this.dir = new File(dirPath);
  }
  
  @Parameterized.Parameters
  public static List directories() {
    return Arrays.asList(new Object[][] {
      { "testRootDir6" },
      { "testRootDir7" }
    });
  }

  @Test
  public void testClone() throws IOException {

    String statementPath = dirPath + File.separator + "statement.yaml";
    
    Vulnerability vuln = Metadata.getFromYaml(statementPath);

    HashMap<String, Object> args = new HashMap<String, Object>();
    args.put("v", false);
    args.put(Import.OVERWRITE_OPTION, false);
    args.put(Import.DIRECTORY_OPTION, "");

    ExtractOrClone extractOrClone = new ExtractOrClone(manager, vuln, this.dir, false);
    extractOrClone.execute();

    File commitDir1 = new File(dirPath + File.separator + "1db7e02de3eb0c011ee6681f5a12eb9d166fea8");
    File commitDir1before = new File(dirPath + File.separator + "1db7e02de3eb0c011ee6681f5a12eb9d166fea8" + File.separator + "before");
    File commitDir1after = new File(dirPath + File.separator + "1db7e02de3eb0c011ee6681f5a12eb9d166fea8" + File.separator + "after");
    File someJavaFile = new File(dirPath + File.separator + "1db7e02de3eb0c011ee6681f5a12eb9d166fea8/after/spring-expression/src/test/java/org/springframework/expression/spel/SpelCompilationCoverageTests.java");
    File commitDir2 = new File(dirPath + File.separator + "d3acf45ea4db51fa5c4cbd0bc0e7b6d9ef805e6");
    File commitDir3 = new File(dirPath + File.separator + "e0de9126ed8cf25cf141d3e66420da94e350708");

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
      System.out.println(e.getMessage());
    }
  }

}
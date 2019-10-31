package com.sap.psr.vulas.git;

import static org.junit.Assert.assertEquals;

import com.sap.psr.vulas.vcs.FileChange;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/*
 * Basic tests of the Git client
 */
public class GitTest {

  private void setupProxy() {

    final String phost = System.getProperty("http.proxyHost");
    final String pport = System.getProperty("http.proxyPort");

    try {
      if (phost != null && pport != null) {
        System.err.println("Using proxy " + phost + ":" + pport);
        ProxySelector.setDefault(
            new MyProxySelector(
                new Proxy(Proxy.Type.HTTP, new InetSocketAddress(phost, Integer.parseInt(pport)))));
      } else {
        System.err.printf("No proxy specified, connecting directly.");
      }
    } catch (Exception e) {
      System.err.println("Error setting proxy");
      e.printStackTrace();
    }
  }

  //	@Test(expected = RepoMismatchException.class)
  //	public void repoMismatchExceptionTest() throws Exception {
  //		this.setupProxy();
  //		GitClient client = new GitClient();
  //		client.setWorkDir(null);
  //		try{
  //		client.setRepoUrl(new URL("https://svn.nonexisting.com"));
  //		} catch(RepoMismatchException rme){
  //			client.cleanup();
  //			throw rme;
  //		}
  //	}

  @Test
  @Category({
    com.sap.psr.vulas.shared.categories.Slow.class,
    com.sap.psr.vulas.shared.categories.RequiresNetwork.class
  })
  public void GitRepositoryTest() {
    try {

      this.setupProxy();
      Map<String, String> results = new HashMap<String, String>();

      GitClient client = new GitClient();
      /* client.setWorkDir( null ); */
      // client.setWorkDir(new File("/tmp/copernico").toPath());
      client.setRepoUrl(new URL("https://github.com/copernico/dotfiles"));

      // search by revision
      Set<String> revisionsToSearch = new HashSet<String>();
      revisionsToSearch.add("b88da5f0c2398b0675ae9823a7140ba689ed70d7");
      revisionsToSearch.add("cf68d5a60c21d1015c5a4572f23e648a4e3242c6");
      results.putAll(client.getCommitLogEntries(revisionsToSearch));

      // search by commit message content
      String commitMessageToSearch = "wrong";
      /* 14 nov 2014: 1415981687 */
      Date searchAfterDate = new Date(1415981687 * 1000L);
      results.putAll(client.searchCommitLog(commitMessageToSearch, searchAfterDate));

      int count = 0;
      for (String key : results.keySet()) {
        count++;
        System.err.println(key);
        System.err.println(results.get(key));
      }
      // assert(count>2);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  @Category({
    com.sap.psr.vulas.shared.categories.Slow.class,
    com.sap.psr.vulas.shared.categories.RequiresNetwork.class
  })
  public void fileChangesCopernicoTest() throws Exception {

    this.setupProxy();
    GitClient client = new GitClient();
    try {
      // client.setWorkDir(new File("/tmp/copernico").toPath());
      client.setRepoUrl(new URL("https://github.com/copernico/dotfiles"));
      /*
       * client.setRepoUrl(new
       * URL("https://github.com/rhuss/jolokia.git"));
       */
    } catch (Exception e) {
    }

    // copernico
    /*
     * String rev = new String( "b88da5f0c2398b0675ae9823a7140ba689ed70d7"
     * );
     */
    /*
     * String rev = new String( "b910bba656e6640b2233340553d32144d5ce3540"
     * );
     */
    /* String rev = new String("4bfdefb2383703bd855ab0f0afc629b045e3344a"); */
    /* String rev = new String("1f00388e961d6e2266dcb06183ec1a1a9fa8490e"); */
    String rev = new String("cf68d5a60c21d1015c5a4572f23e648a4e3242c6");

    Set<FileChange> fileChanges = client.getFileChanges(rev);

    int count = 0;
    for (FileChange c : fileChanges) {
      count++;
      System.err.print("[#" + count + "] ");
      System.err.println(c);
    }
    assertEquals(count, 3);
  }

  /** Test with renamed files */
  @Test
  @Category({
    com.sap.psr.vulas.shared.categories.Slow.class,
    com.sap.psr.vulas.shared.categories.RequiresNetwork.class
  })
  public void renamedFilesTest() throws Exception {

    // rev with renamed files: b88da5f0c2398b0675ae9823a7140ba689ed70d7
    this.setupProxy();
    GitClient client = new GitClient();
    try {
      // client.setWorkDir(new File("/tmp/copernico").toPath());
      client.setRepoUrl(new URL("https://github.com/copernico/dotfiles"));
      /*
       * client.setRepoUrl(new
       * URL("https://github.com/rhuss/jolokia.git"));
       */
    } catch (Exception e) {
    }

    // copernico
    String rev = new String("b88da5f0c2398b0675ae9823a7140ba689ed70d7");
    /*
     * String rev = new String( "b910bba656e6640b2233340553d32144d5ce3540"
     * );
     */
    /* String rev = new String("4bfdefb2383703bd855ab0f0afc629b045e3344a"); */
    /* String rev = new String("1f00388e961d6e2266dcb06183ec1a1a9fa8490e"); */
    /*
     * String rev = new String( "cf68d5a60c21d1015c5a4572f23e648a4e3242c6"
     * );
     */

    Set<FileChange> fileChanges = client.getFileChanges(rev);

    int count = 0;
    for (FileChange c : fileChanges) {
      count++;
      System.err.print("[#" + count + "] ");
      System.err.println(c);
    }

    // IMPORTANT
    // note: there is only 1 change in GIT, but it's a rename,
    // which we represent internally as a remove + add.
    // This is why we expect 2 changes here.
    assertEquals(count, 2);
  }

  /** Must behave correctly if two calls are chained, even if the first fails */
  @Test
  public void chainedOperationTest() throws Exception {

    this.setupProxy();
    GitClient client1 = new GitClient();
    try {
      client1.setWorkDir(new File("/tmp/non-existing").toPath());
      client1.setRepoUrl(new URL("https://svn.nonexisting.domain.blah/nonexisting-repo"));
    } catch (Exception e) {
    }

    client1.cleanup();

    GitClient client2 = new GitClient();
    try {
      // client2.setWorkDir(new File("/tmp/copernico").toPath());
      client2.setRepoUrl(new URL("https://github.com/copernico/dotfiles"));
    } catch (Exception e) {
    }

    // copernico
    /*
     * String rev = new String( "b88da5f0c2398b0675ae9823a7140ba689ed70d7"
     * );
     */
    /*
     * String rev = new String( "b910bba656e6640b2233340553d32144d5ce3540"
     * );
     */
    /* String rev = new String("4bfdefb2383703bd855ab0f0afc629b045e3344a"); */
    /* String rev = new String("1f00388e961d6e2266dcb06183ec1a1a9fa8490e"); */
    String rev = new String("cf68d5a60c21d1015c5a4572f23e648a4e3242c6");

    Set<FileChange> fileChanges = client2.getFileChanges(rev);

    int count = 0;
    for (FileChange c : fileChanges) {
      count++;
      System.err.print("#" + count);
      System.err.println(c);
    }
    assertEquals(count, 3);
  }

  // @Test
  public void fileChangesJolokiaTest() throws Exception {

    this.setupProxy();
    GitClient client = new GitClient();
    try {
      client.setWorkDir(new File("/tmp/patcha_jolokia").toPath());
      client.setRepoUrl(new URL("https://github.com/rhuss/jolokia.git"));
    } catch (Exception e) {
    }

    // Jolokia
    String rev = new String("b9d87e77217191cc4ebd9a581ffa415e05393fb5");
    Set<FileChange> fileChanges = client.getFileChanges(rev);

    /* Set<FileChange> fileChanges = new HashSet<FileChange>(); */
    /* fileChanges = client.getFileChanges( rev ); */

    int count = 0;
    for (FileChange c : fileChanges) {
      count++;
      System.err.println("#" + count);
      System.err.println(c);
    }
    assertEquals(count, 2);
  }
}

package com.sap.psr.vulas.python.virtualenv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.sap.psr.vulas.FileAnalysisException;
import com.sap.psr.vulas.python.ProcessWrapperException;
import com.sap.psr.vulas.python.pip.PipInstalledPackage;
import com.sap.psr.vulas.shared.categories.Slow;
import com.sap.psr.vulas.shared.json.model.ConstructId;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.StringList;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Set;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class VirtualenvWrapperTest {

  /**
   * Attention: Runs long...
   *
   * @throws IllegalArgumentException
   * @throws ProcessWrapperException
   * @throws FileAnalysisException
   */
  @Test
  @Category(Slow.class)
  public void testCreateVirtualenv()
      throws IllegalArgumentException, ProcessWrapperException, FileAnalysisException {

    // Create virtualenv
    final Path project = Paths.get("src", "test", "resources", "cf-helloworld");
    final VirtualenvWrapper vew = new VirtualenvWrapper(project);
    final Path ve_path = vew.getPathToVirtualenv();
    assertTrue(FileUtil.isAccessibleDirectory(ve_path));

    // Get packages
    final Set<PipInstalledPackage> packs = vew.getInstalledPackages();
    assertEquals(8, packs.size());

    // Get rid of the project itself
    final Set<PipInstalledPackage> filtered_packs =
        PipInstalledPackage.filterUsingArtifact(
            packs, new StringList().add("cf-helloworld"), false);
    assertEquals(7, filtered_packs.size());

    // Get SHA1 for every package
    for (PipInstalledPackage p : filtered_packs) {
      final String sha1 = p.getDigest();
      assertTrue(sha1 != null && !sha1.equals(""));
    }

    // Get constructs for every package
    for (PipInstalledPackage p : filtered_packs) {
      final Collection<ConstructId> constructs = p.getLibrary().getConstructs();
      assertTrue(constructs != null && constructs.size() > 0);
    }
  }
}

package com.sap.psr.vulas.mvn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.sap.psr.vulas.shared.json.model.LibraryId;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class AbstractVulasMojoTest {

  @Test
  public void testParseGAPV() {
    final MvnPluginReport m = new MvnPluginReport();
    assertEquals(
        new LibraryId("commons-fileupload", "commons-fileupload", "1.2.1"),
        m.parseGAPV("commons-fileupload:commons-fileupload:jar:1.2.1"));
    assertTrue(m.parseGAPV("commons-fileupload:commons-fileupload:1.2.1") == null);
  }

  @Test
  public void testGetParent() {
    // Construct the dependency trail as provided by Artifact.getDependencyTrail()
    final List<String> trail = new ArrayList<String>();
    trail.add("commons-fileupload:commons-fileupload:jar:1.2.1");
    trail.add("commons-codec:commons-codec:jar:1.2.1");
    trail.add("commons-lang:commons-lang:jar:1.2.1");

    final MvnPluginReport m = new MvnPluginReport();
    final LibraryId parent = m.getParent(trail);
    assertEquals(new LibraryId("commons-codec", "commons-codec", "1.2.1"), parent);
  }
}

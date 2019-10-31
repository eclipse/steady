package com.sap.psr.vulas.cia.rest;

import static org.junit.Assert.assertTrue;

import com.sap.psr.vulas.cia.util.MavenCentralWrapper;
import com.sap.psr.vulas.shared.json.model.Artifact;
import java.util.Set;
import org.junit.Test;

public class IT03_ClassControllerTest {

  @Test
  public void getArtifactsForClassTest() {

    MavenCentralWrapper r = new MavenCentralWrapper();
    try {
      Set<Artifact> response =
          r.getArtifactForClass(
              "org.apache.commons.fileupload.MultipartStream", "1000", null, "jar");

      System.out.println(response.size());
      assertTrue(response.size() >= 172);

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}

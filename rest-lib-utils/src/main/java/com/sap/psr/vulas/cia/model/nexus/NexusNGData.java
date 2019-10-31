package com.sap.psr.vulas.cia.model.nexus;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/** NexusNGData class. */
@XmlRootElement(name = "data")
public class NexusNGData {

  @XmlElement(name = "artifact")
  private List<NexusArtifact> artifact = new ArrayList<NexusArtifact>();

  /**
   * getArtifactList.
   *
   * @return a {@link java.util.List} object.
   */
  public List<NexusArtifact> getArtifactList() {
    return artifact;
  }

  /**
   * setArtifactList.
   *
   * @param _l a {@link java.util.List} object.
   */
  public void setArtifactList(List<NexusArtifact> _l) {
    this.artifact = _l;
  }
}

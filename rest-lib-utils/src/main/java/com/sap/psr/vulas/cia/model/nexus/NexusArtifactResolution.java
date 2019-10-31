package com.sap.psr.vulas.cia.model.nexus;

import javax.xml.bind.annotation.XmlRootElement;

/** NexusArtifactResolution class. */
@XmlRootElement(name = "artifact-resolution") // , namespace="com.concretepage" )
public class NexusArtifactResolution {

  NexusResolvedArtifact data;

  /**
   * Getter for the field <code>data</code>.
   *
   * @return a {@link com.sap.psr.vulas.cia.model.nexus.NexusResolvedArtifact} object.
   */
  public NexusResolvedArtifact getData() {
    return data;
  }

  /**
   * Setter for the field <code>data</code>.
   *
   * @param data a {@link com.sap.psr.vulas.cia.model.nexus.NexusResolvedArtifact} object.
   */
  public void setData(NexusResolvedArtifact data) {
    this.data = data;
  }
}

package com.sap.psr.vulas.cia.model.nexus;

import javax.xml.bind.annotation.XmlRootElement;

/** NexusArtifactInfoResourceResponse class. */
@XmlRootElement(name = "org.sonatype.nexus.rest.model.ArtifactInfoResourceResponse")
public class NexusArtifactInfoResourceResponse {

  NexusDescribeInfo data;

  /**
   * Getter for the field <code>data</code>.
   *
   * @return a {@link com.sap.psr.vulas.cia.model.nexus.NexusDescribeInfo} object.
   */
  public NexusDescribeInfo getData() {
    return data;
  }

  /**
   * Setter for the field <code>data</code>.
   *
   * @param data a {@link com.sap.psr.vulas.cia.model.nexus.NexusDescribeInfo} object.
   */
  public void setData(NexusDescribeInfo data) {
    this.data = data;
  }
}

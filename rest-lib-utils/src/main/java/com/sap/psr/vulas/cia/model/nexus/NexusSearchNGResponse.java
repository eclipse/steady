package com.sap.psr.vulas.cia.model.nexus;

import javax.xml.bind.annotation.XmlRootElement;

/** NexusSearchNGResponse class. */
@XmlRootElement(name = "searchNGResponse")
public class NexusSearchNGResponse {

  NexusNGData data;

  /**
   * Getter for the field <code>data</code>.
   *
   * @return a {@link com.sap.psr.vulas.cia.model.nexus.NexusNGData} object.
   */
  public NexusNGData getData() {
    return data;
  }

  /**
   * Setter for the field <code>data</code>.
   *
   * @param data a {@link com.sap.psr.vulas.cia.model.nexus.NexusNGData} object.
   */
  public void setData(NexusNGData data) {
    this.data = data;
  }
}

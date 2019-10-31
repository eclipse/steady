package com.sap.psr.vulas.cia.model.nexus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collection;

/** NexusSearch class. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NexusSearch {

  private Collection<NexusLibId> data;

  /**
   * Getter for the field <code>data</code>.
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<NexusLibId> getData() {
    return data;
  }

  /**
   * Setter for the field <code>data</code>.
   *
   * @param data a {@link java.util.Collection} object.
   */
  public void setData(Collection<NexusLibId> data) {
    this.data = data;
  }
}

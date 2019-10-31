package com.sap.psr.vulas.cia.model.nexus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collection;

/** NexusResponse class. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NexusResponse {

  private Collection<NexusSearch> searches;

  /** Constructor for NexusResponse. */
  public NexusResponse() {}

  /**
   * Getter for the field <code>searches</code>.
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<NexusSearch> getSearches() {
    return searches;
  }

  /**
   * Setter for the field <code>searches</code>.
   *
   * @param searches a {@link java.util.Collection} object.
   */
  public void setSearches(Collection<NexusSearch> searches) {
    this.searches = searches;
  }
}

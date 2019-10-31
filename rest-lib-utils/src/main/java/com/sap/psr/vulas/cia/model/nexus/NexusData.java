package com.sap.psr.vulas.cia.model.nexus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collection;

/** NexusData class. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NexusData {
  private String instance;

  private String repositoryId;

  private Collection<NexusLibId> gavs;

  /**
   * Getter for the field <code>instance</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getInstance() {
    return instance;
  }

  /**
   * Setter for the field <code>instance</code>.
   *
   * @param instance a {@link java.lang.String} object.
   */
  public void setInstance(String instance) {
    this.instance = instance;
  }

  /**
   * Getter for the field <code>repositoryId</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getRepositoryId() {
    return repositoryId;
  }

  /**
   * Setter for the field <code>repositoryId</code>.
   *
   * @param repositoryId a {@link java.lang.String} object.
   */
  public void setRepositoryId(String repositoryId) {
    this.repositoryId = repositoryId;
  }

  /**
   * Getter for the field <code>gavs</code>.
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<NexusLibId> getGavs() {
    return gavs;
  }

  /**
   * Setter for the field <code>gavs</code>.
   *
   * @param gavs a {@link java.util.Collection} object.
   */
  public void setGavs(Collection<NexusLibId> gavs) {
    this.gavs = gavs;
  }
}

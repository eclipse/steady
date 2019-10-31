package com.sap.psr.vulas.cia.model.nexus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** NexusLibId class. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NexusLibId {

  private String groupId;
  private String artifactId;
  private String version;

  private String repository;

  /** Constructor for NexusLibId. */
  public NexusLibId() {}

  /**
   * Constructor for NexusLibId.
   *
   * @param g a {@link java.lang.String} object.
   * @param a a {@link java.lang.String} object.
   * @param v a {@link java.lang.String} object.
   */
  public NexusLibId(String g, String a, String v) {
    this.groupId = g;
    this.artifactId = a;
    this.version = v;
  }

  /**
   * Getter for the field <code>groupId</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getGroupId() {
    return groupId;
  }
  /**
   * Setter for the field <code>groupId</code>.
   *
   * @param groupId a {@link java.lang.String} object.
   */
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }
  /**
   * Getter for the field <code>artifactId</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getArtifactId() {
    return artifactId;
  }
  /**
   * Setter for the field <code>artifactId</code>.
   *
   * @param artifactId a {@link java.lang.String} object.
   */
  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }
  /**
   * Getter for the field <code>version</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getVersion() {
    return version;
  }
  /**
   * Setter for the field <code>version</code>.
   *
   * @param version a {@link java.lang.String} object.
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * Getter for the field <code>repository</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getRepository() {
    return repository;
  }

  /**
   * Setter for the field <code>repository</code>.
   *
   * @param repository a {@link java.lang.String} object.
   */
  public void setRepository(String repository) {
    this.repository = repository;
  }
}

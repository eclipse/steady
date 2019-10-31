package com.sap.psr.vulas.cia.model.nexus;

import javax.xml.bind.annotation.XmlRootElement;

/** NexusArtifact class. */
@XmlRootElement(name = "artifact")
public class NexusArtifact {

  String groupId;

  String artifactId;

  String version;

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
   * @param artifact a {@link java.lang.String} object.
   */
  public void setArtifactId(String artifact) {
    this.artifactId = artifact;
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

  // It does not look like this field is correctly populated
  // String latestRelease;

}

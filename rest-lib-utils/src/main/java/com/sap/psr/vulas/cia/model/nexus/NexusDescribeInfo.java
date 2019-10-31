package com.sap.psr.vulas.cia.model.nexus;

/** NexusDescribeInfo class. */
public class NexusDescribeInfo {
  Long uploaded;
  Long lastChanged;
  String sha1Hash;

  /**
   * Getter for the field <code>uploaded</code>.
   *
   * @return a {@link java.lang.Long} object.
   */
  public Long getUploaded() {
    return uploaded;
  }
  /**
   * Setter for the field <code>uploaded</code>.
   *
   * @param uploaded a {@link java.lang.Long} object.
   */
  public void setUploaded(Long uploaded) {
    this.uploaded = uploaded;
  }
  /**
   * Getter for the field <code>lastChanged</code>.
   *
   * @return a {@link java.lang.Long} object.
   */
  public Long getLastChanged() {
    return lastChanged;
  }
  /**
   * Setter for the field <code>lastChanged</code>.
   *
   * @param lastChanged a {@link java.lang.Long} object.
   */
  public void setLastChanged(Long lastChanged) {
    this.lastChanged = lastChanged;
  }
  /**
   * Getter for the field <code>sha1Hash</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getSha1Hash() {
    return sha1Hash;
  }
  /**
   * Setter for the field <code>sha1Hash</code>.
   *
   * @param sha1Hash a {@link java.lang.String} object.
   */
  public void setSha1Hash(String sha1Hash) {
    this.sha1Hash = sha1Hash;
  }
}

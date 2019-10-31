package com.sap.psr.vulas.monitor.trace;

import com.sap.psr.vulas.ConstructId;

/** PathNode class. */
public class PathNode {

  private ConstructId constructId = null;

  private String sha1 = null;

  /**
   * Constructor for PathNode.
   *
   * @param _cid a {@link com.sap.psr.vulas.ConstructId} object.
   */
  public PathNode(ConstructId _cid) {
    this(_cid, null);
  }

  /**
   * Constructor for PathNode.
   *
   * @param _cid a {@link com.sap.psr.vulas.ConstructId} object.
   * @param _sha1 a {@link java.lang.String} object.
   */
  public PathNode(ConstructId _cid, String _sha1) {
    this.constructId = _cid;
    this.sha1 = _sha1;
  }

  /**
   * Getter for the field <code>constructId</code>.
   *
   * @return a {@link com.sap.psr.vulas.ConstructId} object.
   */
  public ConstructId getConstructId() {
    return constructId;
  }

  /**
   * Getter for the field <code>sha1</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getSha1() {
    return sha1;
  }

  /**
   * Setter for the field <code>sha1</code>.
   *
   * @param _sha1 a {@link java.lang.String} object.
   */
  public void setSha1(String _sha1) {
    this.sha1 = _sha1;
  }

  /**
   * hasSha1.
   *
   * @return a boolean.
   */
  public boolean hasSha1() {
    return this.sha1 != null;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    final StringBuffer b = new StringBuffer();
    b.append("[qname=");
    b.append(this.constructId.getQualifiedName());
    if (this.sha1 != null) {
      b.append(", libsha1=").append(this.sha1);
    }
    b.append("]");
    return b.toString();
  }
}

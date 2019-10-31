package com.sap.psr.vulas.shared.json.model;

import java.io.Serializable;

/** PathNode class. */
public class PathNode implements Serializable {

  private ConstructId constructId;

  private Library lib;

  /** Constructor for PathNode. */
  public PathNode() {
    super();
  }

  /**
   * Constructor for PathNode.
   *
   * @param _cid a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   */
  public PathNode(ConstructId _cid) {
    this(_cid, null);
  }

  /**
   * Constructor for PathNode.
   *
   * @param _cid a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   * @param _lib a {@link com.sap.psr.vulas.shared.json.model.Library} object.
   */
  public PathNode(ConstructId _cid, Library _lib) {
    super();
    this.constructId = _cid;
    this.lib = _lib;
  }

  /**
   * Getter for the field <code>constructId</code>.
   *
   * @return a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   */
  public ConstructId getConstructId() {
    return constructId;
  }
  /**
   * Setter for the field <code>constructId</code>.
   *
   * @param constructId a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   */
  public void setConstructId(ConstructId constructId) {
    this.constructId = constructId;
  }

  /**
   * Getter for the field <code>lib</code>.
   *
   * @return a {@link com.sap.psr.vulas.shared.json.model.Library} object.
   */
  public Library getLib() {
    return lib;
  }
  /**
   * Setter for the field <code>lib</code>.
   *
   * @param lib a {@link com.sap.psr.vulas.shared.json.model.Library} object.
   */
  public void setLib(Library lib) {
    this.lib = lib;
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((constructId == null) ? 0 : constructId.hashCode());
    result = prime * result + ((lib == null) ? 0 : lib.hashCode());
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    PathNode other = (PathNode) obj;
    if (constructId == null) {
      if (other.constructId != null) return false;
    } else if (!constructId.equals(other.constructId)) return false;
    if (lib == null) {
      if (other.lib != null) return false;
    } else if (!lib.equals(other.lib)) return false;
    return true;
  }
}

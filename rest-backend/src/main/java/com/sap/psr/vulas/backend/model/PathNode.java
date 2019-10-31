package com.sap.psr.vulas.backend.model;

import java.io.Serializable;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/** PathNode class. */
@Embeddable
public class PathNode implements Serializable {

  @ManyToOne(optional = false)
  @JoinColumn(name = "constructId", referencedColumnName = "id")
  private ConstructId constructId;

  @ManyToOne(optional = true)
  @JoinColumn(name = "lib", referencedColumnName = "digest")
  private Library lib;

  @Transient private Dependency dep;

  /** Constructor for PathNode. */
  public PathNode() {
    super();
  }

  /**
   * Constructor for PathNode.
   *
   * @param _cid a {@link com.sap.psr.vulas.backend.model.ConstructId} object.
   */
  public PathNode(ConstructId _cid) {
    this(_cid, null);
  }

  /**
   * Constructor for PathNode.
   *
   * @param _cid a {@link com.sap.psr.vulas.backend.model.ConstructId} object.
   * @param _lib a {@link com.sap.psr.vulas.backend.model.Library} object.
   */
  public PathNode(ConstructId _cid, Library _lib) {
    super();
    this.constructId = _cid;
    this.lib = _lib;
  }

  /**
   * Getter for the field <code>constructId</code>.
   *
   * @return a {@link com.sap.psr.vulas.backend.model.ConstructId} object.
   */
  public ConstructId getConstructId() {
    return constructId;
  }
  /**
   * Setter for the field <code>constructId</code>.
   *
   * @param constructId a {@link com.sap.psr.vulas.backend.model.ConstructId} object.
   */
  public void setConstructId(ConstructId constructId) {
    this.constructId = constructId;
  }

  /**
   * Getter for the field <code>lib</code>.
   *
   * @return a {@link com.sap.psr.vulas.backend.model.Library} object.
   */
  public Library getLib() {
    return lib;
  }
  /**
   * Setter for the field <code>lib</code>.
   *
   * @param lib a {@link com.sap.psr.vulas.backend.model.Library} object.
   */
  public void setLib(Library lib) {
    this.lib = lib;
  }

  /**
   * Getter for the field <code>dep</code>.
   *
   * @return a {@link com.sap.psr.vulas.backend.model.Dependency} object.
   */
  public Dependency getDep() {
    return dep;
  }
  /**
   * Setter for the field <code>dep</code>.
   *
   * @param dep a {@link com.sap.psr.vulas.backend.model.Dependency} object.
   */
  public void setDep(Dependency dep) {
    this.dep = dep;
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

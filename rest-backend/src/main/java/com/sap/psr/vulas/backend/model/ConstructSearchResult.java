package com.sap.psr.vulas.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The result of a search for {@link ConstructId}s in all {@link Dependency}s of an {@link
 * Application}. TODO (HP, 7.3.2017): Check whether it is worthwhile to also create a class
 * ConstructSearch. Right now, all of that is handled in the controller method.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConstructSearchResult implements Comparable {

  private ConstructId constructId = null;

  private Dependency dependency = null;

  /**
   * Constructor for ConstructSearchResult.
   *
   * @param _d a {@link com.sap.psr.vulas.backend.model.Dependency} object.
   * @param _cid a {@link com.sap.psr.vulas.backend.model.ConstructId} object.
   */
  public ConstructSearchResult(Dependency _d, ConstructId _cid) {
    this.constructId = _cid;
    this.dependency = _d;
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
   * Getter for the field <code>dependency</code>.
   *
   * @return a {@link com.sap.psr.vulas.backend.model.Dependency} object.
   */
  public Dependency getDependency() {
    return dependency;
  }

  /**
   * Setter for the field <code>dependency</code>.
   *
   * @param dependency a {@link com.sap.psr.vulas.backend.model.Dependency} object.
   */
  public void setDependency(Dependency dependency) {
    this.dependency = dependency;
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((constructId == null) ? 0 : constructId.hashCode());
    result = prime * result + ((dependency == null) ? 0 : dependency.hashCode());
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ConstructSearchResult other = (ConstructSearchResult) obj;
    if (constructId == null) {
      if (other.constructId != null) return false;
    } else if (!constructId.equals(other.constructId)) return false;
    if (dependency == null) {
      if (other.dependency != null) return false;
    } else if (!dependency.equals(other.dependency)) return false;
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public int compareTo(Object _o) {
    if (!(_o instanceof ConstructSearchResult))
      throw new IllegalArgumentException("Wrong argument type: " + _o.getClass().getName());

    ConstructSearchResult other = (ConstructSearchResult) _o;
    int c = this.getConstructId().compareTo(other.getConstructId());
    if (c == 0)
      c = this.getDependency().getFilename().compareTo(other.getDependency().getFilename());
    return c;
  }
}

package com.sap.psr.vulas.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sap.psr.vulas.shared.enums.ConstructType;
import java.util.Collection;

/**
 * Set of {@link ConstructId}s of type {@link ConstructType#CLAS} that exist in two {@link
 * Dependency}s of an {@link Application}.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DependencyIntersection {

  private Dependency d1 = null;

  private Dependency d2 = null;

  private Collection<ConstructId> constructs = null;

  /**
   * Constructor for DependencyIntersection.
   *
   * @param _d1 a {@link com.sap.psr.vulas.backend.model.Dependency} object.
   * @param _d2 a {@link com.sap.psr.vulas.backend.model.Dependency} object.
   * @param _constructs a long.
   */
  public DependencyIntersection(Dependency _d1, Dependency _d2, long _constructs) {}

  /**
   * Getter for the field <code>d1</code>.
   *
   * @return a {@link com.sap.psr.vulas.backend.model.Dependency} object.
   */
  public Dependency getD1() {
    return d1;
  }

  /**
   * Setter for the field <code>d1</code>.
   *
   * @param d1 a {@link com.sap.psr.vulas.backend.model.Dependency} object.
   */
  public void setD1(Dependency d1) {
    this.d1 = d1;
  }

  /**
   * Getter for the field <code>d2</code>.
   *
   * @return a {@link com.sap.psr.vulas.backend.model.Dependency} object.
   */
  public Dependency getD2() {
    return d2;
  }

  /**
   * Setter for the field <code>d2</code>.
   *
   * @param d2 a {@link com.sap.psr.vulas.backend.model.Dependency} object.
   */
  public void setD2(Dependency d2) {
    this.d2 = d2;
  }

  /**
   * Getter for the field <code>constructs</code>.
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<ConstructId> getConstructs() {
    return constructs;
  }

  /**
   * Setter for the field <code>constructs</code>.
   *
   * @param constructs a {@link java.util.Collection} object.
   */
  public void setConstructs(Collection<ConstructId> constructs) {
    this.constructs = constructs;
  }
}

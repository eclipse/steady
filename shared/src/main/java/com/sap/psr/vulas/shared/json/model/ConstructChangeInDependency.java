package com.sap.psr.vulas.shared.json.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;

/** ConstructChangeInDependency class. */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = false)
public class ConstructChangeInDependency implements Serializable {

  private Trace trace;

  private Boolean traced;
  private Boolean reachable;
  private Boolean inArchive;
  private Boolean affected;
  private Boolean classInArchive;
  private Boolean equalChangeType;
  private String overall_change;

  private ConstructChange constructChange;

  /** Constructor for ConstructChangeInDependency. */
  public ConstructChangeInDependency() {
    super();
  }

  /**
   * Getter for the field <code>constructChange</code>.
   *
   * @return a {@link com.sap.psr.vulas.shared.json.model.ConstructChange} object.
   */
  public ConstructChange getConstructChange() {
    return constructChange;
  }
  /**
   * Setter for the field <code>constructChange</code>.
   *
   * @param constructChange a {@link com.sap.psr.vulas.shared.json.model.ConstructChange} object.
   */
  public void setConstructChange(ConstructChange constructChange) {
    this.constructChange = constructChange;
  }

  /**
   * Getter for the field <code>trace</code>.
   *
   * @return a {@link com.sap.psr.vulas.shared.json.model.Trace} object.
   */
  public Trace getTrace() {
    return trace;
  }
  /**
   * Setter for the field <code>trace</code>.
   *
   * @param trace a {@link com.sap.psr.vulas.shared.json.model.Trace} object.
   */
  public void setTrace(Trace trace) {
    this.trace = trace;
  }

  /**
   * Getter for the field <code>traced</code>.
   *
   * @return a {@link java.lang.Boolean} object.
   */
  public Boolean getTraced() {
    return traced;
  }
  /**
   * Setter for the field <code>traced</code>.
   *
   * @param traced a {@link java.lang.Boolean} object.
   */
  public void setTraced(Boolean traced) {
    this.traced = traced;
  }

  /**
   * Getter for the field <code>inArchive</code>.
   *
   * @return a {@link java.lang.Boolean} object.
   */
  public Boolean getInArchive() {
    return inArchive;
  }
  /**
   * Setter for the field <code>inArchive</code>.
   *
   * @param inArchive a {@link java.lang.Boolean} object.
   */
  public void setInArchive(Boolean inArchive) {
    this.inArchive = inArchive;
  }

  /**
   * Getter for the field <code>reachable</code>.
   *
   * @return a {@link java.lang.Boolean} object.
   */
  public Boolean getReachable() {
    return reachable;
  }
  /**
   * Setter for the field <code>reachable</code>.
   *
   * @param reachable a {@link java.lang.Boolean} object.
   */
  public void setReachable(Boolean reachable) {
    this.reachable = reachable;
  }

  /**
   * isAffected.
   *
   * @return a boolean.
   */
  public boolean isAffected() {
    return affected;
  }

  /**
   * Setter for the field <code>affected</code>.
   *
   * @param affected a boolean.
   */
  public void setAffected(boolean affected) {
    this.affected = affected;
  }

  /**
   * isClassInArchive.
   *
   * @return a boolean.
   */
  public boolean isClassInArchive() {
    return classInArchive;
  }

  /**
   * Setter for the field <code>classInArchive</code>.
   *
   * @param classInArchive a boolean.
   */
  public void setClassInArchive(boolean classInArchive) {
    this.classInArchive = classInArchive;
  }

  /**
   * isEqualChangeType.
   *
   * @return a boolean.
   */
  public boolean isEqualChangeType() {
    return equalChangeType;
  }

  /**
   * Setter for the field <code>equalChangeType</code>.
   *
   * @param equalChangeType a boolean.
   */
  public void setEqualChangeType(boolean equalChangeType) {
    this.equalChangeType = equalChangeType;
  }

  /**
   * Getter for the field <code>overall_change</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getOverall_change() {
    return overall_change;
  }

  /**
   * Setter for the field <code>overall_change</code>.
   *
   * @param overall_change a {@link java.lang.String} object.
   */
  public void setOverall_change(String overall_change) {
    this.overall_change = overall_change;
  }
}

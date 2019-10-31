package com.sap.psr.vulas.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;

/** ConstructChangeInDependency class. */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConstructChangeInDependency implements Serializable {

  private ConstructChange cc;

  // "inarchive": false,
  private Trace trace;
  private Boolean traced;
  /*"reachabilityGraph": null or  {
  "sourceDescription": "APP",
  "shortestpathlength": "5",
  "shortestpathEPcid": "1B06F030C13DA3348473B006824D973F336BB7152BC783C564E415FEE61C8459",
  "id": "16832",
  "shortestpathEP": {
  "lang": "JAVA",
  "type": "METH",
  "qname": "com.acme.ArchivePrinter.compressArchive()"
  }
  }*/
  private Boolean reachable;
  /*"versionCheck": {
  "fixed_version": false,

  "class_in_archive": false,
  "overall_change_type": "ADD"
  }*/

  // TODO
  private Boolean inArchive;
  // fake
  private String reachabilityGraph;

  private Boolean affected;

  private Boolean classInArchive;

  private Boolean equalChangeType;

  private com.sap.psr.vulas.backend.model.AffectedConstructChange.ChangeType overall_change;

  /** Constructor for ConstructChangeInDependency. */
  public ConstructChangeInDependency() {
    super();
  }

  /**
   * Constructor for ConstructChangeInDependency.
   *
   * @param cc a {@link com.sap.psr.vulas.backend.model.ConstructChange} object.
   */
  public ConstructChangeInDependency(ConstructChange cc) {
    super();
    this.cc = cc;
    this.reachabilityGraph = null;
  }

  /**
   * getConstructChange.
   *
   * @return a {@link com.sap.psr.vulas.backend.model.ConstructChange} object.
   */
  public ConstructChange getConstructChange() {
    return cc;
  }
  /**
   * setConstructChange.
   *
   * @param _cc a {@link com.sap.psr.vulas.backend.model.ConstructChange} object.
   */
  public void setConstructChange(ConstructChange _cc) {
    this.cc = _cc;
  }

  /**
   * Getter for the field <code>trace</code>.
   *
   * @return a {@link com.sap.psr.vulas.backend.model.Trace} object.
   */
  public Trace getTrace() {
    return trace;
  }
  /**
   * Setter for the field <code>trace</code>.
   *
   * @param trace a {@link com.sap.psr.vulas.backend.model.Trace} object.
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
   * Getter for the field <code>affected</code>.
   *
   * @return a {@link java.lang.Boolean} object.
   */
  public Boolean getAffected() {
    return affected;
  }
  /**
   * Setter for the field <code>affected</code>.
   *
   * @param a a {@link java.lang.Boolean} object.
   */
  public void setAffected(Boolean a) {
    this.affected = a;
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
   * Getter for the field <code>classInArchive</code>.
   *
   * @return a {@link java.lang.Boolean} object.
   */
  public Boolean getClassInArchive() {
    return classInArchive;
  }
  /**
   * Setter for the field <code>classInArchive</code>.
   *
   * @param classinArchive a {@link java.lang.Boolean} object.
   */
  public void setClassInArchive(Boolean classinArchive) {
    this.classInArchive = classinArchive;
  }

  /**
   * Getter for the field <code>equalChangeType</code>.
   *
   * @return a {@link java.lang.Boolean} object.
   */
  public Boolean getEqualChangeType() {
    return equalChangeType;
  }
  /**
   * Setter for the field <code>equalChangeType</code>.
   *
   * @param e a {@link java.lang.Boolean} object.
   */
  public void setEqualChangeType(Boolean e) {
    this.equalChangeType = e;
  }

  /**
   * Getter for the field <code>overall_change</code>.
   *
   * @return a {@link com.sap.psr.vulas.backend.model.AffectedConstructChange.ChangeType} object.
   */
  public com.sap.psr.vulas.backend.model.AffectedConstructChange.ChangeType getOverall_change() {
    return overall_change;
  }
  /**
   * Setter for the field <code>overall_change</code>.
   *
   * @param changeType a {@link com.sap.psr.vulas.backend.model.AffectedConstructChange.ChangeType}
   *     object.
   */
  public void setOverall_change(
      com.sap.psr.vulas.backend.model.AffectedConstructChange.ChangeType changeType) {
    this.overall_change = changeType;
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
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.psr.vulas.patcheval.representation;

import com.sap.psr.vulas.shared.enums.ConstructChangeType;
import com.sap.psr.vulas.shared.json.model.ConstructId;

/**
 * class representing a consolidated change list (no duplicates because of multiple commits on the
 * same construct)
 */
public class OverallConstructChange {
  String fixedBody, buggyBody;
  ConstructChangeType changetype;
  String repoPath;
  ConstructId constructId;

  /**
   * Constructor for OverallConstructChange.
   *
   * @param fixedBody a {@link java.lang.String} object.
   * @param buggyBody a {@link java.lang.String} object.
   * @param changetype a {@link com.sap.psr.vulas.shared.enums.ConstructChangeType} object.
   * @param repoPath a {@link java.lang.String} object.
   * @param constructId a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   */
  public OverallConstructChange(
      String fixedBody,
      String buggyBody,
      ConstructChangeType changetype,
      String repoPath,
      ConstructId constructId) {
    this.fixedBody = fixedBody;
    this.buggyBody = buggyBody;
    this.changetype = changetype;
    this.repoPath = repoPath;
    this.constructId = constructId;
  }

  /**
   * Getter for the field <code>fixedBody</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getFixedBody() {
    return fixedBody;
  }

  /**
   * Setter for the field <code>fixedBody</code>.
   *
   * @param fixedBody a {@link java.lang.String} object.
   */
  public void setFixedBody(String fixedBody) {
    this.fixedBody = fixedBody;
  }

  /**
   * Getter for the field <code>buggyBody</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getBuggyBody() {
    return buggyBody;
  }

  /**
   * Setter for the field <code>buggyBody</code>.
   *
   * @param buggyBody a {@link java.lang.String} object.
   */
  public void setBuggyBody(String buggyBody) {
    this.buggyBody = buggyBody;
  }

  /**
   * getChangeType.
   *
   * @return a {@link com.sap.psr.vulas.shared.enums.ConstructChangeType} object.
   */
  public ConstructChangeType getChangeType() {
    return changetype;
  }

  /**
   * setChangeType.
   *
   * @param changetype a {@link com.sap.psr.vulas.shared.enums.ConstructChangeType} object.
   */
  public void setChangeType(ConstructChangeType changetype) {
    this.changetype = changetype;
  }

  /**
   * Getter for the field <code>repoPath</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getRepoPath() {
    return repoPath;
  }

  /**
   * Setter for the field <code>repoPath</code>.
   *
   * @param repoPath a {@link java.lang.String} object.
   */
  public void setRepoPath(String repoPath) {
    this.repoPath = repoPath;
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
   * Getter for the field <code>changetype</code>.
   *
   * @return a {@link com.sap.psr.vulas.shared.enums.ConstructChangeType} object.
   */
  public ConstructChangeType getChangetype() {
    return changetype;
  }

  /**
   * Setter for the field <code>changetype</code>.
   *
   * @param changetype a {@link com.sap.psr.vulas.shared.enums.ConstructChangeType} object.
   */
  public void setChangetype(ConstructChangeType changetype) {
    this.changetype = changetype;
  }
}

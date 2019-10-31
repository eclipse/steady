/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.psr.vulas.shared.json.model;

import com.sap.psr.vulas.shared.enums.BugOrigin;
import com.sap.psr.vulas.shared.enums.ContentMaturityLevel;
import com.sap.psr.vulas.shared.json.model.metrics.Metrics;
import java.util.Calendar;
import java.util.Collection;

/** BugChangeList class. */
public class BugChangeList {

  private String bugId;

  private String bugIdAlt = null;

  private ContentMaturityLevel maturity;

  private BugOrigin origin;

  private Float cvssScore = null;

  private String cvssVersion = null;

  private String cvssVector = null;

  private String cvssDisplayString = null;

  private String source;
  private String description;
  private String descriptionAlt;
  private Collection<String> reference;
  private Collection<ConstructChange> constructChanges;
  private Collection<AffectedLibrary> affectedVersions;
  private java.util.Calendar createdAt;
  private String createdBy;
  private java.util.Calendar modifiedAt;
  private Metrics countAffLibIds;

  /**
   * Getter for the field <code>cvssScore</code>.
   *
   * @return a {@link java.lang.Float} object.
   */
  public Float getCvssScore() {
    return cvssScore;
  }

  /**
   * Setter for the field <code>cvssScore</code>.
   *
   * @param cvssScore a {@link java.lang.Float} object.
   */
  public void setCvssScore(Float cvssScore) {
    this.cvssScore = cvssScore;
  }

  /**
   * Getter for the field <code>cvssVersion</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getCvssVersion() {
    return cvssVersion;
  }

  /**
   * Setter for the field <code>cvssVersion</code>.
   *
   * @param cvssVersion a {@link java.lang.String} object.
   */
  public void setCvssVersion(String cvssVersion) {
    this.cvssVersion = cvssVersion;
  }

  /**
   * Getter for the field <code>cvssVector</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getCvssVector() {
    return cvssVector;
  }

  /**
   * Setter for the field <code>cvssVector</code>.
   *
   * @param cvssVector a {@link java.lang.String} object.
   */
  public void setCvssVector(String cvssVector) {
    this.cvssVector = cvssVector;
  }

  /**
   * Getter for the field <code>cvssDisplayString</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getCvssDisplayString() {
    return cvssDisplayString;
  }

  /**
   * Setter for the field <code>cvssDisplayString</code>.
   *
   * @param cvssDisplayString a {@link java.lang.String} object.
   */
  public void setCvssDisplayString(String cvssDisplayString) {
    this.cvssDisplayString = cvssDisplayString;
  }

  /**
   * Getter for the field <code>bugIdAlt</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getBugIdAlt() {
    return bugIdAlt;
  }

  /**
   * Setter for the field <code>bugIdAlt</code>.
   *
   * @param bugIdAlt a {@link java.lang.String} object.
   */
  public void setBugIdAlt(String bugIdAlt) {
    this.bugIdAlt = bugIdAlt;
  }

  /**
   * Getter for the field <code>maturity</code>.
   *
   * @return a {@link com.sap.psr.vulas.shared.enums.ContentMaturityLevel} object.
   */
  public ContentMaturityLevel getMaturity() {
    return maturity;
  }

  /**
   * Setter for the field <code>maturity</code>.
   *
   * @param maturity a {@link com.sap.psr.vulas.shared.enums.ContentMaturityLevel} object.
   */
  public void setMaturity(ContentMaturityLevel maturity) {
    this.maturity = maturity;
  }

  /**
   * Getter for the field <code>origin</code>.
   *
   * @return a {@link com.sap.psr.vulas.shared.enums.BugOrigin} object.
   */
  public BugOrigin getOrigin() {
    return origin;
  }

  /**
   * Setter for the field <code>origin</code>.
   *
   * @param origin a {@link com.sap.psr.vulas.shared.enums.BugOrigin} object.
   */
  public void setOrigin(BugOrigin origin) {
    this.origin = origin;
  }

  /**
   * Getter for the field <code>countAffLibIds</code>.
   *
   * @return a {@link com.sap.psr.vulas.shared.json.model.metrics.Metrics} object.
   */
  public Metrics getCountAffLibIds() {
    return countAffLibIds;
  }

  /**
   * Setter for the field <code>countAffLibIds</code>.
   *
   * @param metrics a {@link com.sap.psr.vulas.shared.json.model.metrics.Metrics} object.
   */
  public void setCountAffLibIds(Metrics metrics) {
    this.countAffLibIds = metrics;
  }

  /**
   * Getter for the field <code>description</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Setter for the field <code>description</code>.
   *
   * @param description a {@link java.lang.String} object.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Getter for the field <code>descriptionAlt</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getDescriptionAlt() {
    return descriptionAlt;
  }

  /**
   * Setter for the field <code>descriptionAlt</code>.
   *
   * @param description a {@link java.lang.String} object.
   */
  public void setDescriptionAlt(String description) {
    this.descriptionAlt = description;
  }

  /**
   * Getter for the field <code>reference</code>.
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<String> getReference() {
    return reference;
  }

  /**
   * Setter for the field <code>reference</code>.
   *
   * @param reference a {@link java.util.Collection} object.
   */
  public void setReference(Collection<String> reference) {
    this.reference = reference;
  }

  /**
   * Getter for the field <code>constructChanges</code>.
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<ConstructChange> getConstructChanges() {
    return constructChanges;
  }

  /**
   * Setter for the field <code>constructChanges</code>.
   *
   * @param constructChanges a {@link java.util.Collection} object.
   */
  public void setConstructChanges(Collection<ConstructChange> constructChanges) {
    this.constructChanges = constructChanges;
  }

  /**
   * Getter for the field <code>affectedVersions</code>.
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<AffectedLibrary> getAffectedVersions() {
    return affectedVersions;
  }

  /**
   * Setter for the field <code>affectedVersions</code>.
   *
   * @param affectedVersions a {@link java.util.Collection} object.
   */
  public void setAffectedVersions(Collection<AffectedLibrary> affectedVersions) {
    this.affectedVersions = affectedVersions;
  }

  /**
   * Getter for the field <code>createdAt</code>.
   *
   * @return a {@link java.util.Calendar} object.
   */
  public Calendar getCreatedAt() {
    return createdAt;
  }

  /**
   * Setter for the field <code>createdAt</code>.
   *
   * @param createdAt a {@link java.util.Calendar} object.
   */
  public void setCreatedAt(Calendar createdAt) {
    this.createdAt = createdAt;
  }

  /**
   * Getter for the field <code>createdBy</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getCreatedBy() {
    return createdBy;
  }

  /**
   * Setter for the field <code>createdBy</code>.
   *
   * @param createdBy a {@link java.lang.String} object.
   */
  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  /**
   * Getter for the field <code>modifiedAt</code>.
   *
   * @return a {@link java.util.Calendar} object.
   */
  public Calendar getModifiedAt() {
    return modifiedAt;
  }

  /**
   * Setter for the field <code>modifiedAt</code>.
   *
   * @param modifiedAt a {@link java.util.Calendar} object.
   */
  public void setModifiedAt(Calendar modifiedAt) {
    this.modifiedAt = modifiedAt;
  }

  /**
   * Getter for the field <code>modifiedBy</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getModifiedBy() {
    return modifiedBy;
  }

  /**
   * Setter for the field <code>modifiedBy</code>.
   *
   * @param modifiedBy a {@link java.lang.String} object.
   */
  public void setModifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
  }

  private String modifiedBy;

  private int countConstructChanges;

  /**
   * Getter for the field <code>bugId</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getBugId() {
    return bugId;
  }

  /**
   * Setter for the field <code>bugId</code>.
   *
   * @param bugId a {@link java.lang.String} object.
   */
  public void setBugId(String bugId) {
    this.bugId = bugId;
  }

  /**
   * Getter for the field <code>countConstructChanges</code>.
   *
   * @return a int.
   */
  public int getCountConstructChanges() {
    return countConstructChanges;
  }

  /**
   * Setter for the field <code>countConstructChanges</code>.
   *
   * @param countConstructChanges a int.
   */
  public void setCountConstructChanges(int countConstructChanges) {
    this.countConstructChanges = countConstructChanges;
  }

  /**
   * Getter for the field <code>source</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getSource() {
    return source;
  }

  /**
   * Setter for the field <code>source</code>.
   *
   * @param source a {@link java.lang.String} object.
   */
  public void setSource(String source) {
    this.source = source;
  }
}

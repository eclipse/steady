/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
package org.eclipse.steady.shared.json.model;

import java.io.Serializable;
import java.util.Collection;

import org.eclipse.steady.shared.enums.BugOrigin;
import org.eclipse.steady.shared.enums.ContentMaturityLevel;
import org.eclipse.steady.shared.json.model.metrics.Metrics;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>Bug class.</p>
 *
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(
    ignoreUnknown = true,
    value = {"affectedVersions", "createdAt", "createdBy"},
    allowGetters = true) // On allowGetters: https://github.com/FasterXML/jackson-databind/issues/95
public class Bug implements Serializable, Comparable {

  private static final long serialVersionUID = 1L;

  /** Constant <code>CVSS_NA="n/a"</code> */
  public static final String CVSS_NA = "n/a";

  @JsonIgnore private Long id;

  private String bugId = null;

  private String bugIdAlt = null;

  private ContentMaturityLevel maturity;

  private BugOrigin origin;

  private Float cvssScore = null;

  private String cvssVersion = null;

  private String cvssVector = null;

  private String cvssDisplayString = null;

  /**
   * Indicates the source of the bug information, e.g., the NVD or vendor-specific advisories.
   * Can be used to collect further information (using the bugId as primary key in the external information source).
   */
  private String source = null;

  private String description = null;

  private String descriptionAlt = null;

  private Collection<String> reference = null;

  @JsonManagedReference private Collection<ConstructChange> constructChanges;

  @JsonManagedReference private Collection<AffectedLibrary> affectedVersions;

  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
      timezone = "GMT")
  private java.util.Calendar createdAt;

  private String createdBy;

  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
      timezone = "GMT")
  private java.util.Calendar modifiedAt;

  private String modifiedBy;

  private Metrics countAffLibIds;

  /**
   * <p>Constructor for Bug.</p>
   */
  public Bug() {
    super();
  }

  /**
   * <p>Constructor for Bug.</p>
   *
   * @param bugId a {@link java.lang.String} object.
   */
  public Bug(String bugId) {
    super();
    this.bugId = bugId;
  }

  /**
   * <p>Constructor for Bug.</p>
   *
   * @param bugId a {@link java.lang.String} object.
   * @param source a {@link java.lang.String} object.
   * @param description a {@link java.lang.String} object.
   * @param refs a {@link java.util.Collection} object.
   */
  public Bug(String bugId, String source, String description, Collection<String> refs) {
    super();
    this.bugId = bugId;
    this.source = source;
    this.description = description;
    this.reference = refs;
  }

  /**
   * <p>Getter for the field <code>reference</code>.</p>
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<String> getReference() {
    return reference;
  }

  /**
   * <p>Setter for the field <code>reference</code>.</p>
   *
   * @param reference a {@link java.util.Collection} object.
   */
  public void setReference(Collection<String> reference) {
    this.reference = reference;
  }

  /**
   * <p>Getter for the field <code>id</code>.</p>
   *
   * @return a {@link java.lang.Long} object.
   */
  public Long getId() {
    return id;
  }
  /**
   * <p>Setter for the field <code>id</code>.</p>
   *
   * @param id a {@link java.lang.Long} object.
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * <p>Getter for the field <code>bugId</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getBugId() {
    return bugId;
  }
  /**
   * <p>Setter for the field <code>bugId</code>.</p>
   *
   * @param bugid a {@link java.lang.String} object.
   */
  public void setBugId(String bugid) {
    this.bugId = bugid;
  }

  /**
   * <p>Getter for the field <code>source</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getSource() {
    return source;
  }
  /**
   * <p>Setter for the field <code>source</code>.</p>
   *
   * @param source a {@link java.lang.String} object.
   */
  public void setSource(String source) {
    this.source = source;
  }

  /**
   * <p>Getter for the field <code>description</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getDescription() {
    return description;
  }
  /**
   * <p>Setter for the field <code>description</code>.</p>
   *
   * @param descr a {@link java.lang.String} object.
   */
  public void setDescription(String descr) {
    this.description = descr;
  }

  /**
   * <p>Getter for the field <code>bugIdAlt</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getBugIdAlt() {
    return bugIdAlt;
  }

  /**
   * <p>Setter for the field <code>bugIdAlt</code>.</p>
   *
   * @param bugIdAlt a {@link java.lang.String} object.
   */
  public void setBugIdAlt(String bugIdAlt) {
    this.bugIdAlt = bugIdAlt;
  }

  /**
   * <p>Getter for the field <code>maturity</code>.</p>
   *
   * @return a {@link org.eclipse.steady.shared.enums.ContentMaturityLevel} object.
   */
  public ContentMaturityLevel getMaturity() {
    return maturity;
  }

  /**
   * <p>Setter for the field <code>maturity</code>.</p>
   *
   * @param maturity a {@link org.eclipse.steady.shared.enums.ContentMaturityLevel} object.
   */
  public void setMaturity(ContentMaturityLevel maturity) {
    this.maturity = maturity;
  }

  /**
   * <p>Getter for the field <code>origin</code>.</p>
   *
   * @return a {@link org.eclipse.steady.shared.enums.BugOrigin} object.
   */
  public BugOrigin getOrigin() {
    return origin;
  }

  /**
   * <p>Setter for the field <code>origin</code>.</p>
   *
   * @param origin a {@link org.eclipse.steady.shared.enums.BugOrigin} object.
   */
  public void setOrigin(BugOrigin origin) {
    this.origin = origin;
  }

  /**
   * <p>Getter for the field <code>cvssScore</code>.</p>
   *
   * @return a {@link java.lang.Float} object.
   */
  public Float getCvssScore() {
    return cvssScore;
  }

  /**
   * <p>Setter for the field <code>cvssScore</code>.</p>
   *
   * @param cvssScore a {@link java.lang.Float} object.
   */
  public void setCvssScore(Float cvssScore) {
    this.cvssScore = cvssScore;
  }

  /**
   * <p>Getter for the field <code>cvssVersion</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getCvssVersion() {
    return cvssVersion;
  }

  /**
   * <p>Setter for the field <code>cvssVersion</code>.</p>
   *
   * @param cvssVersion a {@link java.lang.String} object.
   */
  public void setCvssVersion(String cvssVersion) {
    this.cvssVersion = cvssVersion;
  }

  /**
   * <p>Getter for the field <code>cvssVector</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getCvssVector() {
    return cvssVector;
  }

  /**
   * <p>Setter for the field <code>cvssVector</code>.</p>
   *
   * @param cvssVector a {@link java.lang.String} object.
   */
  public void setCvssVector(String cvssVector) {
    this.cvssVector = cvssVector;
  }

  /**
   * <p>Getter for the field <code>cvssDisplayString</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getCvssDisplayString() {
    return cvssDisplayString;
  }

  /**
   * <p>Setter for the field <code>cvssDisplayString</code>.</p>
   *
   * @param cvssDisplayString a {@link java.lang.String} object.
   */
  public void setCvssDisplayString(String cvssDisplayString) {
    this.cvssDisplayString = cvssDisplayString;
  }

  /**
   * <p>Getter for the field <code>descriptionAlt</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getDescriptionAlt() {
    return descriptionAlt;
  }

  /**
   * <p>Setter for the field <code>descriptionAlt</code>.</p>
   *
   * @param descriptionAlt a {@link java.lang.String} object.
   */
  public void setDescriptionAlt(String descriptionAlt) {
    this.descriptionAlt = descriptionAlt;
  }

  /**
   * <p>Getter for the field <code>constructChanges</code>.</p>
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<ConstructChange> getConstructChanges() {
    return constructChanges;
  }
  /**
   * <p>Setter for the field <code>constructChanges</code>.</p>
   *
   * @param constructChanges a {@link java.util.Collection} object.
   */
  public void setConstructChanges(Collection<ConstructChange> constructChanges) {
    this.constructChanges = constructChanges;
  }

  /**
   * <p>Getter for the field <code>affectedVersions</code>.</p>
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<AffectedLibrary> getAffectedVersions() {
    return affectedVersions;
  }
  /**
   * <p>Setter for the field <code>affectedVersions</code>.</p>
   *
   * @param affectedVersions a {@link java.util.Collection} object.
   */
  public void setAffectedVersions(Collection<AffectedLibrary> affectedVersions) {
    this.affectedVersions = affectedVersions;
  }

  /**
   * <p>Getter for the field <code>createdAt</code>.</p>
   *
   * @return a {@link java.util.Calendar} object.
   */
  public java.util.Calendar getCreatedAt() {
    return createdAt;
  }
  /**
   * <p>Setter for the field <code>createdAt</code>.</p>
   *
   * @param createdAt a {@link java.util.Calendar} object.
   */
  public void setCreatedAt(java.util.Calendar createdAt) {
    this.createdAt = createdAt;
  }

  /**
   * <p>Getter for the field <code>createdBy</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getCreatedBy() {
    return createdBy;
  }
  /**
   * <p>Setter for the field <code>createdBy</code>.</p>
   *
   * @param createdBy a {@link java.lang.String} object.
   */
  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  /**
   * <p>Getter for the field <code>modifiedAt</code>.</p>
   *
   * @return a {@link java.util.Calendar} object.
   */
  public java.util.Calendar getModifiedAt() {
    return modifiedAt;
  }
  /**
   * <p>Setter for the field <code>modifiedAt</code>.</p>
   *
   * @param modifiedAt a {@link java.util.Calendar} object.
   */
  public void setModifiedAt(java.util.Calendar modifiedAt) {
    this.modifiedAt = modifiedAt;
  }

  /**
   * <p>Getter for the field <code>modifiedBy</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getModifiedBy() {
    return modifiedBy;
  }
  /**
   * <p>Setter for the field <code>modifiedBy</code>.</p>
   *
   * @param modifiedBy a {@link java.lang.String} object.
   */
  public void setModifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
  }
  /**
   * <p>Getter for the field <code>countAffLibIds</code>.</p>
   *
   * @return a {@link org.eclipse.steady.shared.json.model.metrics.Metrics} object.
   */
  public Metrics getCountAffLibIds() {
    return countAffLibIds;
  }
  /**
   * <p>Setter for the field <code>countAffLibIds</code>.</p>
   *
   * @param metrics a {@link org.eclipse.steady.shared.json.model.metrics.Metrics} object.
   */
  public void setCountAffLibIds(Metrics metrics) {
    this.countAffLibIds = metrics;
  }

  /**
   * <p>countConstructChanges.</p>
   *
   * @return a int.
   */
  @JsonProperty(value = "countConstructChanges")
  public int countConstructChanges() {
    return (this.getConstructChanges() == null ? -1 : this.getConstructChanges().size());
  }

  /** {@inheritDoc} */
  @Override
  public final String toString() {
    return this.toString(false);
  }

  /**
   * <p>toString.</p>
   *
   * @param _deep a boolean.
   * @return a {@link java.lang.String} object.
   */
  public final String toString(boolean _deep) {
    final StringBuilder builder = new StringBuilder();
    if (_deep) {
      builder
          .append("Bug ")
          .append(this.toString(false))
          .append(System.getProperty("line.separator"));
      for (ConstructChange cc : this.getConstructChanges()) {
        builder.append("    construct change ").append(cc);
        builder
            .append(", construct ID ")
            .append(cc.getConstructId())
            .append(System.getProperty("line.separator"));
      }
    } else {
      builder.append("[").append(this.getId()).append(":").append(this.getBugId()).append("]");
    }
    return builder.toString();
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((bugId == null) ? 0 : bugId.hashCode());
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Bug other = (Bug) obj;
    if (bugId == null) {
      if (other.bugId != null) return false;
    } else if (!bugId.equals(other.bugId)) return false;
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * Compares on the basis of the {@link #bugId}.
   */
  @Override
  public int compareTo(Object o) {
    return this.getBugId().compareToIgnoreCase(((Bug) o).getBugId());
  }
}

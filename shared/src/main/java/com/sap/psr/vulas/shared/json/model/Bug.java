package com.sap.psr.vulas.shared.json.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.psr.vulas.shared.enums.BugOrigin;
import com.sap.psr.vulas.shared.enums.ContentMaturityLevel;
import java.io.Serializable;
import java.util.Collection;

/** Bug class. */
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
   * Indicates the source of the bug information, e.g., the NVD or vendor-specific advisories. Can
   * be used to collect further information (using the bugId as primary key in the external
   * information source).
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

  /** Constructor for Bug. */
  public Bug() {
    super();
  }

  /**
   * Constructor for Bug.
   *
   * @param bugId a {@link java.lang.String} object.
   */
  public Bug(String bugId) {
    super();
    this.bugId = bugId;
  }

  /**
   * Constructor for Bug.
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
   * Getter for the field <code>id</code>.
   *
   * @return a {@link java.lang.Long} object.
   */
  public Long getId() {
    return id;
  }
  /**
   * Setter for the field <code>id</code>.
   *
   * @param id a {@link java.lang.Long} object.
   */
  public void setId(Long id) {
    this.id = id;
  }

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
   * @param bugid a {@link java.lang.String} object.
   */
  public void setBugId(String bugid) {
    this.bugId = bugid;
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
   * @param descr a {@link java.lang.String} object.
   */
  public void setDescription(String descr) {
    this.description = descr;
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
   * @param descriptionAlt a {@link java.lang.String} object.
   */
  public void setDescriptionAlt(String descriptionAlt) {
    this.descriptionAlt = descriptionAlt;
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
  public java.util.Calendar getCreatedAt() {
    return createdAt;
  }
  /**
   * Setter for the field <code>createdAt</code>.
   *
   * @param createdAt a {@link java.util.Calendar} object.
   */
  public void setCreatedAt(java.util.Calendar createdAt) {
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
  public java.util.Calendar getModifiedAt() {
    return modifiedAt;
  }
  /**
   * Setter for the field <code>modifiedAt</code>.
   *
   * @param modifiedAt a {@link java.util.Calendar} object.
   */
  public void setModifiedAt(java.util.Calendar modifiedAt) {
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

  /**
   * countConstructChanges.
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
   * toString.
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
   * <p>Compares on the basis of the {@link #bugId}.
   */
  @Override
  public int compareTo(Object o) {
    return this.getBugId().compareToIgnoreCase(((Bug) o).getBugId());
  }
}

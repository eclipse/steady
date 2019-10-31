package com.sap.psr.vulas.backend.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.sap.psr.vulas.backend.model.view.Views;
import com.sap.psr.vulas.shared.enums.AffectedVersionSource;
import com.sap.psr.vulas.shared.enums.BugOrigin;
import com.sap.psr.vulas.shared.enums.ContentMaturityLevel;
import com.sap.psr.vulas.shared.json.model.metrics.Counter;
import com.sap.psr.vulas.shared.json.model.metrics.Metrics;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

/** Bug class. */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(
    ignoreUnknown = true,
    value = {"createdAt", "createdBy", "modifiedAt"},
    allowGetters = true) // On allowGetters: https://github.com/FasterXML/jackson-databind/issues/95
@Entity
@Table(
    name = "Bug",
    uniqueConstraints = @UniqueConstraint(columnNames = {"bugId"}),
    indexes = {@Index(name = "bugId_index", columnList = "bugId", unique = true)})
public class Bug implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @JsonIgnore
  private Long id;

  @Column(nullable = false, length = 32)
  private String bugId = null;

  @Column(nullable = true, length = 32)
  private String bugIdAlt = null;

  @Column(nullable = false, length = 5)
  @Enumerated(EnumType.STRING)
  private ContentMaturityLevel maturity;

  @Column(nullable = false, length = 6)
  @Enumerated(EnumType.STRING)
  private BugOrigin origin;

  @Column(columnDefinition = "text")
  private String description =
      null; // Can be overwritten with external vuln information, e.g., from official CVE

  @Column(columnDefinition = "text")
  private String descriptionAlt = null;

  @Column(nullable = true)
  private Float cvssScore =
      null; // Can be overwritten with external vuln information, e.g., from official CVE

  @Column(nullable = true, length = 5)
  private String cvssVersion =
      null; // Can be overwritten with external vuln information, e.g., from official CVE

  @Column(nullable = true, length = 100)
  private String cvssVector =
      null; // Can be overwritten with external vuln information, e.g., from official CVE

  @ElementCollection
  @CollectionTable(name = "BugReferences")
  private Collection<String> reference;

  @OneToMany(
      cascade = CascadeType.ALL,
      mappedBy = "bug",
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  @JsonManagedReference
  @JsonView(Views.BugDetails.class)
  private Collection<ConstructChange> constructChanges;

  @OneToMany(
      cascade = {},
      mappedBy = "bugId",
      fetch = FetchType.LAZY)
  // @JsonManagedReference
  @JsonView(Views.Never.class)
  private Collection<AffectedLibrary> affectedVersions;

  @Temporal(TemporalType.TIMESTAMP)
  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
      timezone = "GMT")
  private java.util.Calendar createdAt;

  @Column private String createdBy;

  @Temporal(TemporalType.TIMESTAMP)
  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
      timezone = "GMT")
  private java.util.Calendar modifiedAt;

  @Column private String modifiedBy;

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
   * @param description a {@link java.lang.String} object.
   * @param refs a {@link java.util.Collection} object.
   */
  public Bug(String bugId, String description, Collection<String> refs) {
    super();
    this.bugId = bugId;
    this.description = description;
    this.reference = refs;
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
   * @param bugidAlt a {@link java.lang.String} object.
   */
  public void setBugIdAlt(String bugidAlt) {
    this.bugIdAlt = bugidAlt;
  }

  /**
   * Compares the given String with both {@link #bugId} and {@link #bugIdAlt}.
   *
   * @param _id a {@link java.lang.String} object.
   * @return a boolean.
   */
  @JsonIgnore
  public boolean hasBugId(String _id) {
    return (this.getBugId() != null && this.getBugId().equalsIgnoreCase(_id))
        || (this.getBugIdAlt() != null && this.getBugIdAlt().equalsIgnoreCase(_id));
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
   * @param descr a {@link java.lang.String} object.
   */
  public void setDescriptionAlt(String descr) {
    this.descriptionAlt = descr;
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
   * @param cvss a {@link java.lang.Float} object.
   */
  public void setCvssScore(Float cvss) {
    this.cvssScore = cvss;
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
   * getCvssDisplayString.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getCvssDisplayString() {
    if (this.getCvssScore() == null || this.getCvssVersion() == null)
      return com.sap.psr.vulas.shared.json.model.Bug.CVSS_NA;
    else return this.getCvssScore() + " (v" + this.getCvssVersion() + ")";
  }

  /**
   * getAffLibIdsCounter.
   *
   * @return a {@link com.sap.psr.vulas.shared.json.model.metrics.Metrics} object.
   */
  @JsonProperty(value = "countAffLibIds")
  @JsonView(Views.BugDetails.class)
  public Metrics getAffLibIdsCounter() {
    final Metrics metrics = new Metrics();
    List<Counter> l = new ArrayList<Counter>();
    HashMap<AffectedVersionSource, Integer> avCount = new HashMap<AffectedVersionSource, Integer>();
    // instatiate counter(name,int) based on return value of findAffLIbper Source and Bug
    Collection<AffectedLibrary> c = this.getAffectedVersions();
    if (c != null) {
      for (AffectedLibrary a : c) {
        if (avCount.containsKey(a.getSource())) {
          avCount.put(a.getSource(), avCount.get(a.getSource()) + 1);
        } else {
          avCount.put(a.getSource(), 1);
        }
      }
      for (Entry<AffectedVersionSource, Integer> e : avCount.entrySet()) {
        l.add(new Counter(e.getKey().toString(), e.getValue()));
      }
      metrics.setCounters(l);
    }
    return metrics;
  }

  /**
   * countConstructChanges.
   *
   * @return a int.
   */
  @JsonProperty(value = "countConstructChanges")
  @JsonView(Views.BugDetails.class)
  public int countConstructChanges() {
    return (this.getConstructChanges() == null ? -1 : this.getConstructChanges().size());
  }

  /** prePersist. */
  @PrePersist
  public void prePersist() {
    if (this.getCreatedAt() == null) this.setCreatedAt(Calendar.getInstance());
    this.setModifiedAt(Calendar.getInstance());
  }

  /** preUpdate. */
  @PreUpdate
  public void preUpdate() {
    this.setModifiedAt(Calendar.getInstance());
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
      builder.append("[bugid=").append(this.getBugId());
      if (this.getConstructChanges() != null) {
        builder.append(", #changes=").append(this.getConstructChanges().size());
      }
      builder.append("]");
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
}

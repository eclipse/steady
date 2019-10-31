package com.sap.psr.vulas.backend.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;
import com.sap.psr.vulas.backend.model.view.Views;
import com.sap.psr.vulas.shared.enums.AffectedVersionSource;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

/** AffectedLibrary class. */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(
    ignoreUnknown = true,
    value = {"createdAt", "createdBy"})
@Entity
@Table(
    name = "BugAffectedLibrary",
    uniqueConstraints = {
      @UniqueConstraint(columnNames = {"bugId", "libraryId", "source"}),
      @UniqueConstraint(columnNames = {"bugId", "lib", "source"})
    })
public class AffectedLibrary implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @JsonIgnore
  private Long id;

  @ManyToOne(
      optional = false,
      cascade = {})
  @JoinColumn(name = "bugId", referencedColumnName = "bugId") // Required for the unique constraint
  @JsonView(Views.LibraryIdDetails.class)
  // @JsonBackReference
  private Bug bugId;

  @ManyToOne(
      optional = true,
      cascade = {})
  @JoinColumn(name = "libraryId", referencedColumnName = "id") // Required for the unique constraint
  @JsonView(Views.BugAffLibs.class)
  //	@JsonManagedReference
  private LibraryId libraryId;

  @ManyToOne(
      optional = true,
      cascade = {})
  @JoinColumn(name = "lib", referencedColumnName = "digest") // Required for the unique constraint
  private Library lib;

  @OneToMany(
      cascade = {CascadeType.ALL},
      mappedBy = "affectedLib",
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  @JsonManagedReference
  @JsonView(Views.BugAffLibsDetails.class)
  private Collection<AffectedConstructChange> affectedcc;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private AffectedVersionSource source;

  @Column private Boolean affected;

  @Column private Boolean sourcesAvailable;

  @Temporal(TemporalType.TIMESTAMP)
  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
      timezone = "GMT")
  private java.util.Calendar createdAt;

  @Temporal(TemporalType.TIMESTAMP)
  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
      timezone = "GMT")
  private java.util.Calendar modifiedAt;

  @Column private String createdBy;

  @Column(nullable = true, columnDefinition = "text")
  // @Lob
  private String explanation;

  @Column private String overallConfidence;

  @Column private String pathConfidence;

  @Column private String lastVulnerable;

  @Column private String firstFixed;

  @Column private String fromIntersection;

  @Column private String toIntersection;

  @Column private String ADFixed;

  @Column private String ADPathFixed;

  /** Constructor for AffectedLibrary. */
  public AffectedLibrary() {
    super();
  }

  /**
   * Constructor for AffectedLibrary.
   *
   * @param bug a {@link com.sap.psr.vulas.backend.model.Bug} object.
   * @param libraryId a {@link com.sap.psr.vulas.backend.model.LibraryId} object.
   * @param affected a {@link java.lang.Boolean} object.
   * @param lib a {@link com.sap.psr.vulas.backend.model.Library} object.
   * @param aff_cc a {@link java.util.Collection} object.
   * @param sourceAvailable a {@link java.lang.Boolean} object.
   */
  public AffectedLibrary(
      Bug bug,
      LibraryId libraryId,
      Boolean affected,
      Library lib,
      Collection<AffectedConstructChange> aff_cc,
      Boolean sourceAvailable) {
    super();
    this.bugId = bug;
    this.libraryId = libraryId;
    this.affected = affected;
    this.lib = lib;
    this.affectedcc = aff_cc;
    this.sourcesAvailable = sourceAvailable;
  }

  /**
   * Getter for the field <code>sourcesAvailable</code>.
   *
   * @return a {@link java.lang.Boolean} object.
   */
  public Boolean getSourcesAvailable() {
    return sourcesAvailable;
  }

  /**
   * Setter for the field <code>sourcesAvailable</code>.
   *
   * @param sourceAvailable a {@link java.lang.Boolean} object.
   */
  public void setSourcesAvailable(Boolean sourceAvailable) {
    this.sourcesAvailable = sourceAvailable;
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
   * @return a {@link com.sap.psr.vulas.backend.model.Bug} object.
   */
  public Bug getBugId() {
    return bugId;
  }
  /**
   * Setter for the field <code>bugId</code>.
   *
   * @param bug a {@link com.sap.psr.vulas.backend.model.Bug} object.
   */
  public void setBugId(Bug bug) {
    this.bugId = bug;
  }

  /**
   * Getter for the field <code>libraryId</code>.
   *
   * @return a {@link com.sap.psr.vulas.backend.model.LibraryId} object.
   */
  public LibraryId getLibraryId() {
    return libraryId;
  }
  /**
   * Setter for the field <code>libraryId</code>.
   *
   * @param libraryId a {@link com.sap.psr.vulas.backend.model.LibraryId} object.
   */
  public void setLibraryId(LibraryId libraryId) {
    this.libraryId = libraryId;
  }

  /**
   * Getter for the field <code>lib</code>.
   *
   * @return a {@link com.sap.psr.vulas.backend.model.Library} object.
   */
  public Library getLib() {
    return this.lib;
  }
  /**
   * Setter for the field <code>lib</code>.
   *
   * @param library a {@link com.sap.psr.vulas.backend.model.Library} object.
   */
  public void setLib(Library library) {
    this.lib = library;
  }

  /**
   * Getter for the field <code>affectedcc</code>.
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<AffectedConstructChange> getAffectedcc() {
    return affectedcc;
  }
  /**
   * Setter for the field <code>affectedcc</code>.
   *
   * @param affectedcc a {@link java.util.Collection} object.
   */
  public void setAffectedcc(Collection<AffectedConstructChange> affectedcc) {
    this.affectedcc = affectedcc;
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
   * isAffected.
   *
   * @return a {@link java.lang.Boolean} object.
   */
  public Boolean isAffected() {
    return this.getAffected() == true;
  }
  /**
   * Setter for the field <code>affected</code>.
   *
   * @param affected a {@link java.lang.Boolean} object.
   */
  public void setAffected(Boolean affected) {
    this.affected = affected;
  }

  /**
   * Getter for the field <code>source</code>.
   *
   * @return a {@link com.sap.psr.vulas.shared.enums.AffectedVersionSource} object.
   */
  public AffectedVersionSource getSource() {
    return source;
  }
  /**
   * Setter for the field <code>source</code>.
   *
   * @param source a {@link com.sap.psr.vulas.shared.enums.AffectedVersionSource} object.
   */
  public void setSource(AffectedVersionSource source) {
    this.source = source;
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
   * Getter for the field <code>explanation</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getExplanation() {
    return explanation;
  }
  /**
   * Setter for the field <code>explanation</code>.
   *
   * @param explanation a {@link java.lang.String} object.
   */
  public void setExplanation(String explanation) {
    this.explanation = explanation;
  }

  /**
   * Getter for the field <code>lastVulnerable</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getLastVulnerable() {
    return this.lastVulnerable;
  }
  /**
   * Setter for the field <code>lastVulnerable</code>.
   *
   * @param lv a {@link java.lang.String} object.
   */
  public void setLastVulnerable(String lv) {
    this.lastVulnerable = lv;
  }

  /**
   * Getter for the field <code>firstFixed</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getFirstFixed() {
    return this.firstFixed;
  }
  /**
   * Setter for the field <code>firstFixed</code>.
   *
   * @param ff a {@link java.lang.String} object.
   */
  public void setFirstFixed(String ff) {
    this.firstFixed = ff;
  }

  /**
   * Getter for the field <code>fromIntersection</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getFromIntersection() {
    return this.fromIntersection;
  }
  /**
   * Setter for the field <code>fromIntersection</code>.
   *
   * @param fi a {@link java.lang.String} object.
   */
  public void setFromIntersection(String fi) {
    this.fromIntersection = fi;
  }

  /**
   * Getter for the field <code>toIntersection</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getToIntersection() {
    return this.toIntersection;
  }
  /**
   * Setter for the field <code>toIntersection</code>.
   *
   * @param ti a {@link java.lang.String} object.
   */
  public void setToIntersection(String ti) {
    this.toIntersection = ti;
  }

  /**
   * Getter for the field <code>overallConfidence</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getOverallConfidence() {
    return overallConfidence;
  }

  /**
   * Setter for the field <code>overallConfidence</code>.
   *
   * @param overallConfidence a {@link java.lang.String} object.
   */
  public void setOverallConfidence(String overallConfidence) {
    this.overallConfidence = overallConfidence;
  }

  /**
   * Getter for the field <code>pathConfidence</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getPathConfidence() {
    return pathConfidence;
  }

  /**
   * Setter for the field <code>pathConfidence</code>.
   *
   * @param pathConfidence a {@link java.lang.String} object.
   */
  public void setPathConfidence(String pathConfidence) {
    this.pathConfidence = pathConfidence;
  }

  /**
   * getADFixed.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getADFixed() {
    return ADFixed;
  }

  /**
   * setADFixed.
   *
   * @param aDFixed a {@link java.lang.String} object.
   */
  public void setADFixed(String aDFixed) {
    ADFixed = aDFixed;
  }

  /**
   * getADPathFixed.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getADPathFixed() {
    return ADPathFixed;
  }

  /**
   * setADPathFixed.
   *
   * @param aDPathFixed a {@link java.lang.String} object.
   */
  public void setADPathFixed(String aDPathFixed) {
    ADPathFixed = aDPathFixed;
  }

  /** prePersist. */
  @PrePersist
  public void prePersist() {
    if (this.getCreatedAt() == null) {
      this.setCreatedAt(Calendar.getInstance());
    }
    if (this.getModifiedAt() == null) {
      this.setModifiedAt(Calendar.getInstance());
    }
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((bugId == null) ? 0 : bugId.hashCode());
    result = prime * result + ((libraryId == null) ? 0 : libraryId.hashCode());
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    AffectedLibrary other = (AffectedLibrary) obj;
    if (bugId == null) {
      if (other.bugId != null) return false;
    } else if (!bugId.equals(other.bugId)) return false;
    if (libraryId == null) {
      if (other.libraryId != null) return false;
    } else if (!libraryId.equals(other.libraryId)) return false;
    return true;
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
          .append("Library affected: [")
          .append(this.getAffected())
          .append("]")
          .append(System.getProperty("line.separator"));
      builder.append("    Bug ").append(this.getBugId());
      if (this.getLibraryId() != null) builder.append("    LibraryId ").append(this.getLibraryId());
      if (this.getLib() != null) builder.append("    Library ").append(this.getLib());
    } else {
      builder
          .append("[")
          .append("bugid=")
          .append(this.getBugId().getBugId())
          .append(", affected=")
          .append(this.getAffected())
          .append(", source=")
          .append(this.getSource().toString());
      if (this.getLibraryId() != null) {
        builder
            .append(", libid=")
            .append(this.getLibraryId().getMvnGroup())
            .append(":")
            .append(this.getLibraryId().getArtifact())
            .append(":")
            .append(this.getLibraryId().getVersion());
      }
      if (this.getLib() != null) builder.append(", lib digest=").append(this.getLib().getDigest());
      builder.append("]");
    }
    return builder.toString();
  }
}

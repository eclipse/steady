package com.sap.psr.vulas.shared.json.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sap.psr.vulas.shared.enums.AffectedVersionSource;
import java.io.Serializable;

/** AffectedLibrary class. */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(
    ignoreUnknown = true,
    value = {"createdAt", "createdBy"})
public class AffectedLibrary implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonIgnore private Long id;

  @JsonBackReference private Bug bugId;

  private LibraryId libraryId;

  private Library lib;

  private AffectedVersionSource source;

  private Boolean affected;

  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
      timezone = "GMT")
  private java.util.Calendar createdAt;

  private String createdBy;

  private String explanation;

  /** Constructor for AffectedLibrary. */
  public AffectedLibrary() {
    super();
  }

  /**
   * Constructor for AffectedLibrary.
   *
   * @param bug a {@link com.sap.psr.vulas.shared.json.model.Bug} object.
   * @param libraryId a {@link com.sap.psr.vulas.shared.json.model.LibraryId} object.
   * @param affected a {@link java.lang.Boolean} object.
   */
  public AffectedLibrary(Bug bug, LibraryId libraryId, Boolean affected) {
    super();
    this.bugId = bug;
    this.libraryId = libraryId;
    this.affected = affected;
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
   * @return a {@link com.sap.psr.vulas.shared.json.model.Bug} object.
   */
  public Bug getBugId() {
    return bugId;
  }
  /**
   * Setter for the field <code>bugId</code>.
   *
   * @param bug a {@link com.sap.psr.vulas.shared.json.model.Bug} object.
   */
  public void setBugId(Bug bug) {
    this.bugId = bug;
  }

  /**
   * Getter for the field <code>libraryId</code>.
   *
   * @return a {@link com.sap.psr.vulas.shared.json.model.LibraryId} object.
   */
  public LibraryId getLibraryId() {
    return libraryId;
  }
  /**
   * Setter for the field <code>libraryId</code>.
   *
   * @param libraryId a {@link com.sap.psr.vulas.shared.json.model.LibraryId} object.
   */
  public void setLibraryId(LibraryId libraryId) {
    this.libraryId = libraryId;
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

  /**
   * Getter for the field <code>lib</code>.
   *
   * @return a {@link com.sap.psr.vulas.shared.json.model.Library} object.
   */
  public Library getLib() {
    return lib;
  }

  /**
   * Setter for the field <code>lib</code>.
   *
   * @param lib a {@link com.sap.psr.vulas.shared.json.model.Library} object.
   */
  public void setLib(Library lib) {
    this.lib = lib;
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
      builder.append("    LibraryId ").append(this.getLibraryId());
    } else {
      builder
          .append("[")
          .append(this.getId())
          .append(":")
          .append(this.getBugId())
          .append(":affected=")
          .append(this.getAffected())
          .append("]");
    }
    return builder.toString();
  }
}

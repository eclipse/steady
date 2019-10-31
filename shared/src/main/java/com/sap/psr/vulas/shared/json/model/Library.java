package com.sap.psr.vulas.shared.json.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.sap.psr.vulas.shared.enums.DigestAlgorithm;
import com.sap.psr.vulas.shared.json.model.view.Views;
import java.io.Serializable;
import java.util.Collection;

/** Library class. */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(
    ignoreUnknown = true,
    value = {"constructCounter", "constructTypeCounters"},
    allowGetters = true)
public class Library implements Serializable {

  private static final long serialVersionUID = 1L;

  private String digest;

  private DigestAlgorithm digestAlgorithm;

  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
      timezone = "GMT")
  private java.util.Calendar createdAt;

  @JsonView(Views.LibDetails.class)
  private Collection<Property> properties;

  @JsonView(Views.LibDetails.class)
  private Collection<ConstructId> constructs;

  /**
   * Library identifier used for publishing the library in package repositories such as Maven
   * Central or PyPI.
   */
  private LibraryId libraryId;

  /**
   * Other library identifiers found within the library. Could be many in case a a library bundles
   * (includes) the content of several other libraries (e.g., Uber JARs).
   */
  private Collection<LibraryId> bundledLibraryIds;

  /**
   * True if the library provider or a trusted software repository confirms the mapping of SHA1 to
   * human-readable ID, false otherwise.
   */
  private Boolean wellknownDigest;

  /**
   * The URL used to verify the digest. Will be empty if none of the available package repositories
   * was able to confirm the digest.
   */
  @JsonIgnoreProperties(
      value = {"digestVerificationUrl"},
      allowGetters = true)
  private String digestVerificationUrl;

  /** Constructor for Library. */
  public Library() {
    super();
  }

  /**
   * Constructor for Library.
   *
   * @param digest a {@link java.lang.String} object.
   */
  public Library(String digest) {
    super();
    this.digest = digest;
  }

  /**
   * Getter for the field <code>digest</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getDigest() {
    return digest;
  }
  /**
   * Setter for the field <code>digest</code>.
   *
   * @param digest a {@link java.lang.String} object.
   */
  public void setDigest(String digest) {
    this.digest = digest;
  }

  /**
   * Getter for the field <code>digestAlgorithm</code>.
   *
   * @return a {@link com.sap.psr.vulas.shared.enums.DigestAlgorithm} object.
   */
  public DigestAlgorithm getDigestAlgorithm() {
    return digestAlgorithm;
  }
  /**
   * Setter for the field <code>digestAlgorithm</code>.
   *
   * @param digestAlgorithm a {@link com.sap.psr.vulas.shared.enums.DigestAlgorithm} object.
   */
  public void setDigestAlgorithm(DigestAlgorithm digestAlgorithm) {
    this.digestAlgorithm = digestAlgorithm;
  }

  /**
   * Returns true if the library has a digest and a digest algorithm, false otherwise.
   *
   * @return a boolean.
   */
  public boolean hasValidDigest() {
    return this.getDigest() != null && this.getDigestAlgorithm() != null;
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
   * Getter for the field <code>properties</code>.
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<Property> getProperties() {
    return properties;
  }
  /**
   * Setter for the field <code>properties</code>.
   *
   * @param properties a {@link java.util.Collection} object.
   */
  public void setProperties(Collection<Property> properties) {
    this.properties = properties;
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
   * @param _library_id a {@link com.sap.psr.vulas.shared.json.model.LibraryId} object.
   */
  public void setLibraryId(LibraryId _library_id) {
    this.libraryId = _library_id;
  }

  /**
   * Getter for the field <code>bundledLibraryIds</code>.
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<LibraryId> getBundledLibraryIds() {
    return this.bundledLibraryIds;
  }
  /**
   * Setter for the field <code>bundledLibraryIds</code>.
   *
   * @param _libids a {@link java.util.Collection} object.
   */
  public void setBundledLibraryIds(Collection<LibraryId> _libids) {
    this.bundledLibraryIds = _libids;
  }

  /**
   * isWellknownDigest.
   *
   * @return a boolean.
   */
  public boolean isWellknownDigest() {
    return wellknownDigest != null && wellknownDigest.equals(true);
  }
  /**
   * Getter for the field <code>wellknownDigest</code>.
   *
   * @return a {@link java.lang.Boolean} object.
   */
  public Boolean getWellknownDigest() {
    return wellknownDigest;
  }
  /**
   * Setter for the field <code>wellknownDigest</code>.
   *
   * @param wellknownDigest a {@link java.lang.Boolean} object.
   */
  public void setWellknownDigest(Boolean wellknownDigest) {
    this.wellknownDigest = wellknownDigest;
  }

  /**
   * Getter for the field <code>digestVerificationUrl</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getDigestVerificationUrl() {
    return digestVerificationUrl;
  }
  /**
   * Setter for the field <code>digestVerificationUrl</code>.
   *
   * @param digestVerificationUrl a {@link java.lang.String} object.
   */
  public void setDigestVerificationUrl(String digestVerificationUrl) {
    this.digestVerificationUrl = digestVerificationUrl;
  }

  /**
   * countConstructs.
   *
   * @return a int.
   */
  @JsonProperty(value = "constructCounter")
  @JsonView(Views.LibDetails.class)
  public int countConstructs() {
    return (this.getConstructs() == null ? 0 : this.getConstructs().size());
  }

  /**
   * countConstructTypes.
   *
   * @return a {@link com.sap.psr.vulas.shared.json.model.ConstructIdFilter} object.
   */
  @JsonProperty(value = "constructTypeCounters")
  @JsonView(Views.LibDetails.class)
  public ConstructIdFilter countConstructTypes() {
    return new ConstructIdFilter(this.getConstructs());
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((digest == null) ? 0 : digest.hashCode());
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Library other = (Library) obj;
    if (digest == null) {
      if (other.digest != null) return false;
    } else if (!digest.equals(other.digest)) return false;
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public final String toString() {
    return this.toString(false);
  }

  /**
   * Returns a short or long string representation of the library.
   *
   * @param _deep a boolean.
   * @return a {@link java.lang.String} object.
   */
  public final String toString(boolean _deep) {
    final StringBuilder builder = new StringBuilder();
    if (_deep) {
      builder
          .append("Library ")
          .append(this.toString(false))
          .append(System.getProperty("line.separator"));
      for (ConstructId cid : this.getConstructs()) {
        builder
            .append("    ConstructId     ")
            .append(cid)
            .append(System.getProperty("line.separator"));
      }
    } else {
      builder.append("[").append(this.getDigest()).append("]");
    }
    return builder.toString();
  }
}

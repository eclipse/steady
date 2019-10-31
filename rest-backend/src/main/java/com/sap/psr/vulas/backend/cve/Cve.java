package com.sap.psr.vulas.backend.cve;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Used for (de)serializing the JSON read from circl.lu */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Cve {

  /** Constant <code>CVE_REGEX="(CVE-\\d{4}+-\\d{4,}).*"</code> */
  public static final String CVE_REGEX = "(CVE-\\d{4}+-\\d{4,}).*";
  /** Constant <code>CVE_PATTERN</code> */
  public static final Pattern CVE_PATTERN = Pattern.compile(CVE_REGEX);

  @JsonProperty(value = "id")
  private String id;

  @JsonProperty(value = "Published")
  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
      timezone = "GMT")
  private java.util.Calendar published;

  @JsonProperty(value = "Modified")
  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
      timezone = "GMT")
  private java.util.Calendar modified;

  @JsonProperty(value = "cvss")
  private Float cvssScore = null;

  @JsonProperty(value = "cvssVersion")
  private String cvssVersion = null;

  @JsonProperty(value = "cvssVector")
  private String cvssVector = null;

  @JsonProperty(value = "summary")
  private String summary;

  /** Constructor for Cve. */
  public Cve() {
    super();
  }

  /**
   * Getter for the field <code>id</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getId() {
    return id;
  }
  /**
   * Setter for the field <code>id</code>.
   *
   * @param id a {@link java.lang.String} object.
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Getter for the field <code>published</code>.
   *
   * @return a {@link java.util.Calendar} object.
   */
  public java.util.Calendar getPublished() {
    return published;
  }
  /**
   * Setter for the field <code>published</code>.
   *
   * @param published a {@link java.util.Calendar} object.
   */
  public void setPublished(java.util.Calendar published) {
    this.published = published;
  }

  /**
   * Getter for the field <code>modified</code>.
   *
   * @return a {@link java.util.Calendar} object.
   */
  public java.util.Calendar getModified() {
    return modified;
  }
  /**
   * Setter for the field <code>modified</code>.
   *
   * @param modified a {@link java.util.Calendar} object.
   */
  public void setModified(java.util.Calendar modified) {
    this.modified = modified;
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
   * Getter for the field <code>summary</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getSummary() {
    return summary;
  }
  /**
   * Setter for the field <code>summary</code>.
   *
   * @param summary a {@link java.lang.String} object.
   */
  public void setSummary(String summary) {
    this.summary = summary;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    final StringBuffer b = new StringBuffer();
    b.append("[id=").append(this.getId()).append(", cvss=").append(this.getCvssScore()).append("]");
    return b.toString();
  }

  /**
   * Uses {@link Cve#CVE_REGEX} to extract a CVE identifier from the given {@link String}. Returns
   * null if no such identifier can be found.
   *
   * @param _string a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String extractCveIdentifier(String _string) {
    if (_string == null) return null;
    final Matcher m = CVE_PATTERN.matcher(_string.toUpperCase());
    if (m.matches()) return m.group(1);
    else return null;
  }
}

package com.sap.psr.vulas.shared.json.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sap.psr.vulas.shared.enums.ExportConfiguration;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/**
 * A space is an isolated environment within a given {@link Tenant}. Every application scan has to
 * happen in the context of a space. The scan of an {@link Application} in one space is entirely
 * independent of the scan of the same {@link Application} in another space.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Space implements Serializable {

  public String spaceToken;

  public String spaceName;

  public String spaceDescription;

  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
      timezone = "GMT")
  private java.util.Calendar createdAt;

  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
      timezone = "GMT")
  private java.util.Calendar lastModified;

  /** Configures the export of scan results. */
  private ExportConfiguration exportConfiguration = ExportConfiguration.AGGREGATED;

  /** Determines whether the space is visible in a public directory. */
  private boolean isPublic = true;

  /** Determines whether the space is the default one. */
  private boolean isDefault = false;

  /** Determines whether the space is read-only. */
  private boolean isReadOnly = false;

  /**
   * Determines the vulnerabilities that should be considered by the scans done in the scope of this
   * space.
   */
  private int bugFilter = -1;

  /** Email address of the space owner(s) */
  private Set<String> ownerEmails = null;

  private Collection<Property> properties;

  /** Constructor for Space. */
  public Space() {}

  /**
   * Constructor for Space.
   *
   * @param _t a {@link java.lang.String} object.
   * @param _n a {@link java.lang.String} object.
   * @param _d a {@link java.lang.String} object.
   */
  public Space(String _t, String _n, String _d) {
    this.setSpaceToken(_t);
    this.setSpaceName(_n);
    this.setSpaceDescription(_d);
  }

  /**
   * Getter for the field <code>spaceToken</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getSpaceToken() {
    return spaceToken;
  }
  /**
   * Setter for the field <code>spaceToken</code>.
   *
   * @param spaceToken a {@link java.lang.String} object.
   */
  public void setSpaceToken(String spaceToken) {
    this.spaceToken = spaceToken;
  }

  /**
   * isValidSpaceToken.
   *
   * @return a boolean.
   */
  @JsonIgnore
  public boolean isValidSpaceToken() {
    return this.spaceToken != null && !this.spaceToken.equals("");
  }

  /**
   * Getter for the field <code>spaceName</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getSpaceName() {
    return spaceName;
  }
  /**
   * Setter for the field <code>spaceName</code>.
   *
   * @param spaceName a {@link java.lang.String} object.
   */
  public void setSpaceName(String spaceName) {
    this.spaceName = spaceName;
  }

  /**
   * Getter for the field <code>spaceDescription</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getSpaceDescription() {
    return spaceDescription;
  }
  /**
   * Setter for the field <code>spaceDescription</code>.
   *
   * @param spaceDescription a {@link java.lang.String} object.
   */
  public void setSpaceDescription(String spaceDescription) {
    this.spaceDescription = spaceDescription;
  }

  /**
   * hasNameAndDescription.
   *
   * @return a boolean.
   */
  public boolean hasNameAndDescription() {
    return this.spaceName != null
        && this.spaceDescription != null
        && !this.spaceName.equals("")
        && !this.spaceDescription.equals("");
  }

  /**
   * Getter for the field <code>exportConfiguration</code>.
   *
   * @return a {@link com.sap.psr.vulas.shared.enums.ExportConfiguration} object.
   */
  public ExportConfiguration getExportConfiguration() {
    return exportConfiguration;
  }
  /**
   * Setter for the field <code>exportConfiguration</code>.
   *
   * @param exportConfiguration a {@link com.sap.psr.vulas.shared.enums.ExportConfiguration} object.
   */
  public void setExportConfiguration(ExportConfiguration exportConfiguration) {
    this.exportConfiguration = exportConfiguration;
  }

  /**
   * isPublic.
   *
   * @return a boolean.
   */
  public boolean isPublic() {
    return isPublic;
  }
  /**
   * setPublic.
   *
   * @param isPublic a boolean.
   */
  public void setPublic(boolean isPublic) {
    this.isPublic = isPublic;
  }

  /**
   * Getter for the field <code>bugFilter</code>.
   *
   * @return a int.
   */
  public int getBugFilter() {
    return bugFilter;
  }
  /**
   * Setter for the field <code>bugFilter</code>.
   *
   * @param bugFilter a int.
   */
  public void setBugFilter(int bugFilter) {
    this.bugFilter = bugFilter;
  }

  /**
   * Getter for the field <code>ownerEmails</code>.
   *
   * @return a {@link java.util.Set} object.
   */
  public Set<String> getOwnerEmails() {
    return ownerEmails;
  }
  /**
   * Setter for the field <code>ownerEmails</code>.
   *
   * @param ownerEmails a {@link java.util.Set} object.
   */
  public void setOwnerEmails(Set<String> ownerEmails) {
    this.ownerEmails = ownerEmails;
  }

  /**
   * toString.
   *
   * @return a {@link java.lang.String} object.
   */
  public String toString() {
    return this.spaceToken;
  }

  /**
   * isDefault.
   *
   * @return a boolean.
   */
  public boolean isDefault() {
    return isDefault;
  }
  /**
   * setDefault.
   *
   * @param isDefault a boolean.
   */
  public void setDefault(boolean isDefault) {
    this.isDefault = isDefault;
  }

  /**
   * isReadOnly.
   *
   * @return a boolean.
   */
  public boolean isReadOnly() {
    return isReadOnly;
  }
  /**
   * setReadOnly.
   *
   * @param isReadOnly a boolean.
   */
  public void setReadOnly(boolean isReadOnly) {
    this.isReadOnly = isReadOnly;
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

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((spaceToken == null) ? 0 : spaceToken.hashCode());
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Space other = (Space) obj;
    if (spaceToken == null) {
      if (other.spaceToken != null) return false;
    } else if (!spaceToken.equals(other.spaceToken)) return false;
    return true;
  }
}

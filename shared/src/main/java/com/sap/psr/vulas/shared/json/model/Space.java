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
package com.sap.psr.vulas.shared.json.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sap.psr.vulas.shared.enums.ExportConfiguration;

/**
 * A space is an isolated environment within a given {@link Tenant}. Every application scan has to happen in the context of a space.
 * The scan of an {@link Application} in one space is entirely independent of the scan of the same {@link Application} in another space.
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

  /**
   * Configures the export of scan results.
   */
  private ExportConfiguration exportConfiguration = ExportConfiguration.AGGREGATED;

  /**
   * Determines whether the space is visible in a public directory.
   */
  private boolean isPublic = true;

  /**
   * Determines whether the space is the default one.
   */
  private boolean isDefault = false;

  /**
   * Determines whether the space is read-only.
   */
  private boolean isReadOnly = false;

  /**
   * Determines the vulnerabilities that should be considered by the scans done in the scope of this space.
   */
  private int bugFilter = -1;

  /**
   * Email address of the space owner(s)
   */
  private Set<String> ownerEmails = null;

  private Collection<Property> properties;

  /**
   * <p>Constructor for Space.</p>
   */
  public Space() {}

  /**
   * <p>Constructor for Space.</p>
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
   * <p>Getter for the field <code>spaceToken</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getSpaceToken() {
    return spaceToken;
  }
  /**
   * <p>Setter for the field <code>spaceToken</code>.</p>
   *
   * @param spaceToken a {@link java.lang.String} object.
   */
  public void setSpaceToken(String spaceToken) {
    this.spaceToken = spaceToken;
  }

  /**
   * <p>isValidSpaceToken.</p>
   *
   * @return a boolean.
   */
  @JsonIgnore
  public boolean isValidSpaceToken() {
    return this.spaceToken != null && !this.spaceToken.equals("");
  }

  /**
   * <p>Getter for the field <code>spaceName</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getSpaceName() {
    return spaceName;
  }
  /**
   * <p>Setter for the field <code>spaceName</code>.</p>
   *
   * @param spaceName a {@link java.lang.String} object.
   */
  public void setSpaceName(String spaceName) {
    this.spaceName = spaceName;
  }

  /**
   * <p>Getter for the field <code>spaceDescription</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getSpaceDescription() {
    return spaceDescription;
  }
  /**
   * <p>Setter for the field <code>spaceDescription</code>.</p>
   *
   * @param spaceDescription a {@link java.lang.String} object.
   */
  public void setSpaceDescription(String spaceDescription) {
    this.spaceDescription = spaceDescription;
  }

  /**
   * <p>hasNameAndDescription.</p>
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
   * <p>Getter for the field <code>exportConfiguration</code>.</p>
   *
   * @return a {@link com.sap.psr.vulas.shared.enums.ExportConfiguration} object.
   */
  public ExportConfiguration getExportConfiguration() {
    return exportConfiguration;
  }
  /**
   * <p>Setter for the field <code>exportConfiguration</code>.</p>
   *
   * @param exportConfiguration a {@link com.sap.psr.vulas.shared.enums.ExportConfiguration} object.
   */
  public void setExportConfiguration(ExportConfiguration exportConfiguration) {
    this.exportConfiguration = exportConfiguration;
  }

  /**
   * <p>isPublic.</p>
   *
   * @return a boolean.
   */
  public boolean isPublic() {
    return isPublic;
  }
  /**
   * <p>setPublic.</p>
   *
   * @param isPublic a boolean.
   */
  public void setPublic(boolean isPublic) {
    this.isPublic = isPublic;
  }

  /**
   * <p>Getter for the field <code>bugFilter</code>.</p>
   *
   * @return a int.
   */
  public int getBugFilter() {
    return bugFilter;
  }
  /**
   * <p>Setter for the field <code>bugFilter</code>.</p>
   *
   * @param bugFilter a int.
   */
  public void setBugFilter(int bugFilter) {
    this.bugFilter = bugFilter;
  }

  /**
   * <p>Getter for the field <code>ownerEmails</code>.</p>
   *
   * @return a {@link java.util.Set} object.
   */
  public Set<String> getOwnerEmails() {
    return ownerEmails;
  }
  /**
   * <p>Setter for the field <code>ownerEmails</code>.</p>
   *
   * @param ownerEmails a {@link java.util.Set} object.
   */
  public void setOwnerEmails(Set<String> ownerEmails) {
    this.ownerEmails = ownerEmails;
  }

  /**
   * <p>toString.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String toString() {
    return this.spaceToken;
  }

  /**
   * <p>isDefault.</p>
   *
   * @return a boolean.
   */
  public boolean isDefault() {
    return isDefault;
  }
  /**
   * <p>setDefault.</p>
   *
   * @param isDefault a boolean.
   */
  public void setDefault(boolean isDefault) {
    this.isDefault = isDefault;
  }

  /**
   * <p>isReadOnly.</p>
   *
   * @return a boolean.
   */
  public boolean isReadOnly() {
    return isReadOnly;
  }
  /**
   * <p>setReadOnly.</p>
   *
   * @param isReadOnly a boolean.
   */
  public void setReadOnly(boolean isReadOnly) {
    this.isReadOnly = isReadOnly;
  }

  /**
   * <p>Getter for the field <code>properties</code>.</p>
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<Property> getProperties() {
    return properties;
  }
  /**
   * <p>Setter for the field <code>properties</code>.</p>
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

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
package com.sap.psr.vulas.backend.cve;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Used for (de)serializing the JSON read from circl.lu
 */
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

  /**
   * <p>Constructor for Cve.</p>
   */
  public Cve() {
    super();
  }

  /**
   * <p>Getter for the field <code>id</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getId() {
    return id;
  }
  /**
   * <p>Setter for the field <code>id</code>.</p>
   *
   * @param id a {@link java.lang.String} object.
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * <p>Getter for the field <code>published</code>.</p>
   *
   * @return a {@link java.util.Calendar} object.
   */
  public java.util.Calendar getPublished() {
    return published;
  }
  /**
   * <p>Setter for the field <code>published</code>.</p>
   *
   * @param published a {@link java.util.Calendar} object.
   */
  public void setPublished(java.util.Calendar published) {
    this.published = published;
  }

  /**
   * <p>Getter for the field <code>modified</code>.</p>
   *
   * @return a {@link java.util.Calendar} object.
   */
  public java.util.Calendar getModified() {
    return modified;
  }
  /**
   * <p>Setter for the field <code>modified</code>.</p>
   *
   * @param modified a {@link java.util.Calendar} object.
   */
  public void setModified(java.util.Calendar modified) {
    this.modified = modified;
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
   * @param cvss a {@link java.lang.Float} object.
   */
  public void setCvssScore(Float cvss) {
    this.cvssScore = cvss;
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
   * <p>Getter for the field <code>summary</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getSummary() {
    return summary;
  }
  /**
   * <p>Setter for the field <code>summary</code>.</p>
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
    b.append("[id=")
        .append(this.getId())
        .append(", cvssScore=")
        .append(this.getCvssScore())
        .append(", cvssVersion=")
        .append(this.getCvssVersion())
        .append("]");
    return b.toString();
  }

  /**
   * Uses {@link Cve#CVE_REGEX} to extract a CVE identifier from the given {@link String}.
   * Returns null if no such identifier can be found.
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

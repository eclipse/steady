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
package org.eclipse.steady.cia.model.pypi;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * <p>PypiInfo class.</p>
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PypiInfo {

  String maintainer_email;

  String package_url;

  String author;

  String author_email;

  String download_url;

  String version;

  String release_url;

  List<String> classifiers;

  String name;

  String bugtrack_url;

  String license;

  String summary;

  String home_page;

  /**
   * <p>Getter for the field <code>maintainer_email</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getMaintainer_email() {
    return maintainer_email;
  }

  /**
   * <p>Setter for the field <code>maintainer_email</code>.</p>
   *
   * @param maintainer_email a {@link java.lang.String} object.
   */
  public void setMaintainer_email(String maintainer_email) {
    this.maintainer_email = maintainer_email;
  }

  /**
   * <p>Getter for the field <code>package_url</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getPackage_url() {
    return package_url;
  }

  /**
   * <p>Setter for the field <code>package_url</code>.</p>
   *
   * @param package_url a {@link java.lang.String} object.
   */
  public void setPackage_url(String package_url) {
    this.package_url = package_url;
  }

  /**
   * <p>Getter for the field <code>author</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getAuthor() {
    return author;
  }

  /**
   * <p>Setter for the field <code>author</code>.</p>
   *
   * @param author a {@link java.lang.String} object.
   */
  public void setAuthor(String author) {
    this.author = author;
  }

  /**
   * <p>Getter for the field <code>author_email</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getAuthor_email() {
    return author_email;
  }

  /**
   * <p>Setter for the field <code>author_email</code>.</p>
   *
   * @param author_email a {@link java.lang.String} object.
   */
  public void setAuthor_email(String author_email) {
    this.author_email = author_email;
  }

  /**
   * <p>Getter for the field <code>download_url</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getDownload_url() {
    return download_url;
  }

  /**
   * <p>Setter for the field <code>download_url</code>.</p>
   *
   * @param download_url a {@link java.lang.String} object.
   */
  public void setDownload_url(String download_url) {
    this.download_url = download_url;
  }

  /**
   * <p>Getter for the field <code>version</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getVersion() {
    return version;
  }

  /**
   * <p>Setter for the field <code>version</code>.</p>
   *
   * @param version a {@link java.lang.String} object.
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * <p>Getter for the field <code>release_url</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getRelease_url() {
    return release_url;
  }

  /**
   * <p>Setter for the field <code>release_url</code>.</p>
   *
   * @param release_url a {@link java.lang.String} object.
   */
  public void setRelease_url(String release_url) {
    this.release_url = release_url;
  }

  /**
   * <p>Getter for the field <code>classifiers</code>.</p>
   *
   * @return a {@link java.util.List} object.
   */
  public List<String> getClassifiers() {
    return classifiers;
  }

  /**
   * <p>Setter for the field <code>classifiers</code>.</p>
   *
   * @param classifiers a {@link java.util.List} object.
   */
  public void setClassifiers(List<String> classifiers) {
    this.classifiers = classifiers;
  }

  /**
   * <p>Getter for the field <code>name</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getName() {
    return name;
  }

  /**
   * <p>Setter for the field <code>name</code>.</p>
   *
   * @param name a {@link java.lang.String} object.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * <p>Getter for the field <code>bugtrack_url</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getBugtrack_url() {
    return bugtrack_url;
  }

  /**
   * <p>Setter for the field <code>bugtrack_url</code>.</p>
   *
   * @param bugtrack_url a {@link java.lang.String} object.
   */
  public void setBugtrack_url(String bugtrack_url) {
    this.bugtrack_url = bugtrack_url;
  }

  /**
   * <p>Getter for the field <code>license</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getLicense() {
    return license;
  }

  /**
   * <p>Setter for the field <code>license</code>.</p>
   *
   * @param license a {@link java.lang.String} object.
   */
  public void setLicense(String license) {
    this.license = license;
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

  /**
   * <p>Getter for the field <code>home_page</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getHome_page() {
    return home_page;
  }

  /**
   * <p>Setter for the field <code>home_page</code>.</p>
   *
   * @param home_page a {@link java.lang.String} object.
   */
  public void setHome_page(String home_page) {
    this.home_page = home_page;
  }
}

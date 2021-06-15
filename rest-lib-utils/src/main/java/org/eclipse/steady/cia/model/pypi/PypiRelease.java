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
 * SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
 */
package org.eclipse.steady.cia.model.pypi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * <p>PypiRelease class.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PypiRelease {

  String upload_time;
  String python_version;
  String url;
  String md5_digest;
  String filename;

  /**
   * <p>Getter for the field <code>upload_time</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getUpload_time() {
    return upload_time;
  }
  /**
   * <p>Setter for the field <code>upload_time</code>.</p>
   *
   * @param upload_time a {@link java.lang.String} object.
   */
  public void setUpload_time(String upload_time) {
    this.upload_time = upload_time;
  }
  /**
   * <p>Getter for the field <code>python_version</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getPython_version() {
    return python_version;
  }
  /**
   * <p>Setter for the field <code>python_version</code>.</p>
   *
   * @param python_version a {@link java.lang.String} object.
   */
  public void setPython_version(String python_version) {
    this.python_version = python_version;
  }
  /**
   * <p>Getter for the field <code>url</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getUrl() {
    return url;
  }
  /**
   * <p>Setter for the field <code>url</code>.</p>
   *
   * @param url a {@link java.lang.String} object.
   */
  public void setUrl(String url) {
    this.url = url;
  }
  /**
   * <p>Getter for the field <code>md5_digest</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getMd5_digest() {
    return md5_digest;
  }
  /**
   * <p>Setter for the field <code>md5_digest</code>.</p>
   *
   * @param md5_digest a {@link java.lang.String} object.
   */
  public void setMd5_digest(String md5_digest) {
    this.md5_digest = md5_digest;
  }
  /**
   * <p>Getter for the field <code>filename</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getFilename() {
    return filename;
  }
  /**
   * <p>Setter for the field <code>filename</code>.</p>
   *
   * @param filename a {@link java.lang.String} object.
   */
  public void setFilename(String filename) {
    this.filename = filename;
  }
  /**
   * <p>Getter for the field <code>packagetype</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getPackagetype() {
    return packagetype;
  }
  /**
   * <p>Setter for the field <code>packagetype</code>.</p>
   *
   * @param packagetype a {@link java.lang.String} object.
   */
  public void setPackagetype(String packagetype) {
    this.packagetype = packagetype;
  }
  /**
   * <p>Getter for the field <code>path</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getPath() {
    return path;
  }
  /**
   * <p>Setter for the field <code>path</code>.</p>
   *
   * @param path a {@link java.lang.String} object.
   */
  public void setPath(String path) {
    this.path = path;
  }
  /**
   * <p>Getter for the field <code>size</code>.</p>
   *
   * @return a long.
   */
  public long getSize() {
    return size;
  }
  /**
   * <p>Setter for the field <code>size</code>.</p>
   *
   * @param size a long.
   */
  public void setSize(long size) {
    this.size = size;
  }

  String packagetype;
  String path;
  long size;
}

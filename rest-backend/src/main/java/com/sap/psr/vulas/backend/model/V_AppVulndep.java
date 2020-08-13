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
package com.sap.psr.vulas.backend.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * <p>V_AppVulndep class.</p>
 *
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@IdClass(V_AppVulndep.class)
@Table(
    name = "v_app_vulndep",
    uniqueConstraints =
        @UniqueConstraint(columnNames = {"app_id", "dep_id", "lib_id", "digest", "bug"}))
public class V_AppVulndep implements Serializable {

  @Id private Long app_id;

  @Column private String app_group;

  @Column private String app_artifact;

  @Column private String app_version;

  @Id private Long dep_id;

  @Id private String bug;

  @Id private Long lib_id;

  @Id private String digest;

  @Column private String filename;

  @Column private String scope;

  @Column private Boolean transitive;

  @Column private String mvn_group;

  @Column private String artifact;

  @Column private String version;

  @Column private Boolean affected;

  /**
   * <p>Constructor for V_AppVulndep.</p>
   */
  public V_AppVulndep() {
    super();
  }

  /**
   * <p>Getter for the field <code>app_id</code>.</p>
   *
   * @return a {@link java.lang.Long} object.
   */
  public Long getApp_id() {
    return app_id;
  }

  /**
   * <p>Setter for the field <code>app_id</code>.</p>
   *
   * @param app_id a {@link java.lang.Long} object.
   */
  public void setApp_id(Long app_id) {
    this.app_id = app_id;
  }

  /**
   * <p>Getter for the field <code>dep_id</code>.</p>
   *
   * @return a {@link java.lang.Long} object.
   */
  public Long getDep_id() {
    return dep_id;
  }

  /**
   * <p>Setter for the field <code>dep_id</code>.</p>
   *
   * @param dep_id a {@link java.lang.Long} object.
   */
  public void setDep_id(Long dep_id) {
    this.dep_id = dep_id;
  }

  /**
   * <p>Getter for the field <code>lib_id</code>.</p>
   *
   * @return a {@link java.lang.Long} object.
   */
  public Long getLib_id() {
    return lib_id;
  }

  /**
   * <p>Setter for the field <code>lib_id</code>.</p>
   *
   * @param lib_id a {@link java.lang.Long} object.
   */
  public void setLib_id(Long lib_id) {
    this.lib_id = lib_id;
  }

  /**
   * <p>Setter for the field <code>bug</code>.</p>
   *
   * @param bug a {@link java.lang.String} object.
   */
  public void setBug(String bug) {
    this.bug = bug;
  }

  /**
   * <p>Getter for the field <code>app_group</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getApp_group() {
    return app_group;
  }

  /**
   * <p>Setter for the field <code>app_group</code>.</p>
   *
   * @param app_group a {@link java.lang.String} object.
   */
  public void setApp_group(String app_group) {
    this.app_group = app_group;
  }

  /**
   * <p>Getter for the field <code>app_artifact</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getApp_artifact() {
    return app_artifact;
  }

  /**
   * <p>Setter for the field <code>app_artifact</code>.</p>
   *
   * @param app_artifact a {@link java.lang.String} object.
   */
  public void setApp_artifact(String app_artifact) {
    this.app_artifact = app_artifact;
  }

  /**
   * <p>Getter for the field <code>app_version</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getApp_version() {
    return app_version;
  }

  /**
   * <p>Setter for the field <code>app_version</code>.</p>
   *
   * @param app_version a {@link java.lang.String} object.
   */
  public void setApp_version(String app_version) {
    this.app_version = app_version;
  }

  /**
   * <p>Getter for the field <code>bug</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getBug() {
    return bug;
  }

  /**
   * <p>setBugId.</p>
   *
   * @param bug a {@link java.lang.String} object.
   */
  public void setBugId(String bug) {
    this.bug = bug;
  }

  /**
   * <p>Getter for the field <code>digest</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getDigest() {
    return digest;
  }

  /**
   * <p>Setter for the field <code>digest</code>.</p>
   *
   * @param digest a {@link java.lang.String} object.
   */
  public void setDigest(String digest) {
    this.digest = digest;
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
   * <p>Getter for the field <code>mvn_group</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getMvn_group() {
    return mvn_group;
  }

  /**
   * <p>Setter for the field <code>mvn_group</code>.</p>
   *
   * @param mvn_group a {@link java.lang.String} object.
   */
  public void setMvn_group(String mvn_group) {
    this.mvn_group = mvn_group;
  }

  /**
   * <p>Getter for the field <code>artifact</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getArtifact() {
    return artifact;
  }

  /**
   * <p>Setter for the field <code>artifact</code>.</p>
   *
   * @param artifact a {@link java.lang.String} object.
   */
  public void setArtifact(String artifact) {
    this.artifact = artifact;
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
   * <p>Getter for the field <code>affected</code>.</p>
   *
   * @return a {@link java.lang.Boolean} object.
   */
  public Boolean getAffected() {
    return affected;
  }

  /**
   * <p>Setter for the field <code>affected</code>.</p>
   *
   * @param affected a {@link java.lang.Boolean} object.
   */
  public void setAffected(Boolean affected) {
    this.affected = affected;
  }
}

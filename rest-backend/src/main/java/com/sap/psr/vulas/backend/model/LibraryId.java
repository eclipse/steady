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
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.sap.psr.vulas.backend.model.view.Views;

/**
 * Human-readable library ID, e.g., a Maven artifact identifier.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(
    name = "LibraryId",
    uniqueConstraints = @UniqueConstraint(columnNames = {"mvnGroup", "artifact", "version"}))
// @JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class, property="@id")
public class LibraryId implements Serializable, Comparable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @JsonIgnore
  private Long id;

  @Column(nullable = false, length = 512)
  @JsonProperty("group")
  private String mvnGroup;

  @Column(nullable = false, length = 512)
  private String artifact;

  @Column(nullable = false, length = 128)
  private String version;

  @OneToMany(mappedBy = "libraryId")
  @JsonView(Views.LibraryIdDetails.class)
  //	@JsonBackReference
  private Collection<AffectedLibrary> affLibraries;

  /**
   * <p>Constructor for LibraryId.</p>
   */
  public LibraryId() {
    super();
  }

  /**
   * <p>Constructor for LibraryId.</p>
   *
   * @param group a {@link java.lang.String} object.
   * @param artifact a {@link java.lang.String} object.
   * @param version a {@link java.lang.String} object.
   */
  public LibraryId(String group, String artifact, String version) {
    super();
    this.mvnGroup = group;
    this.artifact = artifact;
    this.version = version;
  }

  /**
   * <p>Getter for the field <code>id</code>.</p>
   *
   * @return a {@link java.lang.Long} object.
   */
  public Long getId() {
    return id;
  }
  /**
   * <p>Setter for the field <code>id</code>.</p>
   *
   * @param id a {@link java.lang.Long} object.
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * <p>Getter for the field <code>mvnGroup</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getMvnGroup() {
    return mvnGroup;
  }
  /**
   * <p>Setter for the field <code>mvnGroup</code>.</p>
   *
   * @param group a {@link java.lang.String} object.
   */
  public void setMvnGroup(String group) {
    this.mvnGroup = group;
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
   * <p>Getter for the field <code>affLibraries</code>.</p>
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<AffectedLibrary> getAffLibraries() {
    return affLibraries;
  }
  /**
   * <p>Setter for the field <code>affLibraries</code>.</p>
   *
   * @param a a {@link java.util.Collection} object.
   */
  public void setAffLibraries(Collection<AffectedLibrary> a) {
    this.affLibraries = a;
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((artifact == null) ? 0 : artifact.hashCode());
    result = prime * result + ((mvnGroup == null) ? 0 : mvnGroup.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    LibraryId other = (LibraryId) obj;
    if (artifact == null) {
      if (other.artifact != null) return false;
    } else if (!artifact.equals(other.artifact)) return false;
    if (mvnGroup == null) {
      if (other.mvnGroup != null) return false;
    } else if (!mvnGroup.equals(other.mvnGroup)) return false;
    if (version == null) {
      if (other.version != null) return false;
    } else if (!version.equals(other.version)) return false;
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public final String toString() {
    final StringBuilder builder = new StringBuilder();
    builder
        .append("[")
        .append(this.getId())
        .append(":")
        .append(this.getMvnGroup())
        .append("|")
        .append(this.getArtifact())
        .append("|")
        .append(this.getVersion())
        .append("]");
    return builder.toString();
  }

  /**
   * Returns true if the given {@link LibraryId} only differs in the version while group and artifact are identical, false otherwise.
   *
   * @param obj a {@link com.sap.psr.vulas.backend.model.LibraryId} object.
   * @return a boolean.
   */
  public boolean equalsButVersion(LibraryId obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    LibraryId other = (LibraryId) obj;
    if (artifact == null) {
      if (other.artifact != null) return false;
    } else if (!artifact.equals(other.artifact)) return false;
    if (mvnGroup == null) {
      if (other.mvnGroup != null) return false;
    } else if (!mvnGroup.equals(other.mvnGroup)) return false;
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public int compareTo(Object _other) {
    if (_other == null || !(_other instanceof LibraryId)) throw new IllegalArgumentException();
    int v = this.getMvnGroup().compareTo(((LibraryId) _other).getMvnGroup());
    if (v == 0) v = this.getArtifact().compareTo(((LibraryId) _other).getArtifact());
    if (v == 0) v = this.getVersion().compareTo(((LibraryId) _other).getVersion());
    return v;
  }

  /**
   * <p>toSharedType.</p>
   *
   * @return a {@link com.sap.psr.vulas.shared.json.model.LibraryId} object.
   */
  public com.sap.psr.vulas.shared.json.model.LibraryId toSharedType() {
    return new com.sap.psr.vulas.shared.json.model.LibraryId(
        this.mvnGroup, this.artifact, this.version);
  }
}

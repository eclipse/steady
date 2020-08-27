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
package org.eclipse.steady.backend.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.eclipse.steady.backend.model.view.Views;
import org.eclipse.steady.backend.rest.ApplicationController;
import org.eclipse.steady.shared.util.Constants;
import org.eclipse.steady.shared.util.StringUtil;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

/**
 * <p>Application class.</p>
 *
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(
    name = "App",
    uniqueConstraints =
        @UniqueConstraint(columnNames = {"space", "mvnGroup", "artifact", "version"}))
public class Application implements Serializable, Comparable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "space", referencedColumnName = "id")
  @JsonBackReference // Required in order to omit the tenant property when de-serializing JSON
  private Space space = null;

  @Column(nullable = false, length = Constants.MAX_LENGTH_GROUP)
  @JsonProperty("group")
  private String mvnGroup;

  @Column(nullable = false, length = Constants.MAX_LENGTH_ARTIFACT)
  private String artifact;

  @Column(nullable = false, length = Constants.MAX_LENGTH_VERSION)
  private String version;

  @Temporal(TemporalType.TIMESTAMP)
  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
      timezone = "GMT")
  @JsonIgnoreProperties(
      value = {"createdAt"},
      allowGetters = true)
  private java.util.Calendar createdAt;

  // timestamp of the last application modification (customSave operation on the Application entity
  // to store application constructs and/or dependencies)
  @Temporal(TemporalType.TIMESTAMP)
  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
      timezone = "GMT")
  @JsonIgnoreProperties(
      value = {"modifiedAt"},
      allowGetters = true)
  private java.util.Calendar modifiedAt;

  // timestamp of the last application scan (customSave operation on the GoalExecution entity to
  // store the details about a starting/completed scan--triggered by the client)
  @Temporal(TemporalType.TIMESTAMP)
  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
      timezone = "GMT")
  @JsonIgnoreProperties(
      value = {"lastScan"},
      allowGetters = true) // TODO: to remove if the value should be given by the client
  private java.util.Calendar lastScan;

  // timestamp of the last change on the vulnerabilities that may affect the application results,
  // i.e., a change either in the bugs or affected libraries, thus vulndeps for the application
  // needs to be queried to get updated results
  @Temporal(TemporalType.TIMESTAMP)
  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
      timezone = "GMT")
  @JsonIgnoreProperties(
      value = {"lastVulnChange"},
      allowGetters = true)
  private java.util.Calendar lastVulnChange;

  @ManyToMany(
      cascade = {},
      fetch = FetchType.LAZY)
  @JsonView(Views.Never.class)
  private Collection<ConstructId> constructs;

  @OneToMany(
      cascade = {CascadeType.ALL},
      fetch = FetchType.LAZY,
      mappedBy = "app",
      orphanRemoval = true)
  @JsonManagedReference // (value="app-deps")
  @JsonView(Views.AppDepDetails.class)
  private Collection<Dependency> dependencies;

  @Transient private PackageStatistics packageStats = null;

  /**
   * Only set when single applications are returned by {@link ApplicationController#getApplication(String, String, String)}.
   * TODO: Maybe check if they can always bet set (depending on performance and memory).
   */
  @Transient private Collection<Trace> traces;

  /**
   * Contains collections of traced constructs per application package.
   */
  @Transient private PackageStatistics tracedPackageStats = null;

  /**
   * <p>Constructor for Application.</p>
   */
  public Application() {
    super();
  }

  /**
   * <p>Constructor for Application.</p>
   *
   * @param group a {@link java.lang.String} object.
   * @param artifact a {@link java.lang.String} object.
   * @param version a {@link java.lang.String} object.
   */
  public Application(String group, String artifact, String version) {
    super();
    try {
      this.setMvnGroup(group);
      this.setArtifact(artifact);
      this.setVersion(version);
    } catch (IllegalArgumentException iae) {
      throw new IllegalArgumentException(
          "Arguments provided cannot be used to create an application identifier. Group and"
              + " artifact must be specified and cannot exceed "
              + Constants.MAX_LENGTH_GROUP
              + " characters, version must be specified and cannot exceed "
              + Constants.MAX_LENGTH_VERSION
              + " characters");
    }
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
   * <p>Getter for the field <code>space</code>.</p>
   *
   * @return a {@link org.eclipse.steady.backend.model.Space} object.
   */
  public Space getSpace() {
    return space;
  }
  /**
   * <p>Setter for the field <code>space</code>.</p>
   *
   * @param space a {@link org.eclipse.steady.backend.model.Space} object.
   */
  public void setSpace(Space space) {
    this.space = space;
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
    if (StringUtil.meetsLengthConstraint(group, Constants.MAX_LENGTH_GROUP)) this.mvnGroup = group;
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
    if (StringUtil.meetsLengthConstraint(artifact, Constants.MAX_LENGTH_GROUP))
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
    if (StringUtil.meetsLengthConstraint(version, Constants.MAX_LENGTH_GROUP))
      this.version = version;
  }

  /**
   * <p>Getter for the field <code>constructs</code>.</p>
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<ConstructId> getConstructs() {
    return constructs;
  }
  /**
   * <p>Setter for the field <code>constructs</code>.</p>
   *
   * @param constructs a {@link java.util.Collection} object.
   */
  public void setConstructs(Collection<ConstructId> constructs) {
    this.constructs = constructs;
  }

  /**
   * <p>Getter for the field <code>traces</code>.</p>
   *
   * @return a {@link java.util.Collection} object.
   */
  @JsonIgnore
  public Collection<Trace> getTraces() {
    return traces;
  }

  /**
   * <p>Setter for the field <code>traces</code>.</p>
   *
   * @param traces a {@link java.util.Collection} object.
   */
  @JsonIgnore
  public void setTraces(Collection<Trace> traces) {
    this.traces = traces;
  }

  /**
   * <p>getDependency.</p>
   *
   * @param _digest a {@link java.lang.String} object.
   * @return a {@link org.eclipse.steady.backend.model.Dependency} object.
   */
  public Dependency getDependency(@NotNull String _digest) {
    if (this.getDependencies() != null) {
      for (Dependency d : this.getDependencies()) {
        if (d.getLib() != null && d.getLib().getDigest().equals(_digest)) {
          return d;
        }
      }
    }
    return null;
  }
  /**
   * <p>Getter for the field <code>dependencies</code>.</p>
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<Dependency> getDependencies() {
    return dependencies;
  }
  /**
   * <p>Setter for the field <code>dependencies</code>.</p>
   *
   * @param _dependencies a {@link java.util.Collection} object.
   */
  public void setDependencies(Collection<Dependency> _dependencies) {
    if (this.dependencies == null) this.dependencies = _dependencies;
    else {
      // Get around exception: "org.hibernate.HibernateException: A collection with
      // cascade="all-delete-orphan" was no longer referenced by the owning entity instance:"
      this.dependencies.clear();
      this.dependencies.addAll(_dependencies);
    }
  }

  /**
   * <p>Orders the collection <code>dependencies</code> from the shallowest (i.e., direct dependencies that do not have any parent) to the deepest (i.e., the dependencies having the longest chain of parents).</p>
   *
   */
  public void orderDependenciesByDepth() {
    // order dependencies by length of parents
    List<Dependency> ordered_deps = new ArrayList<Dependency>(this.getDependencies());
    Comparator<Dependency> comparator =
        new Comparator<Dependency>() {
          @Override
          public int compare(Dependency left, Dependency right) {
            int l = 0, r = 0;
            while (left.getParent() != null) {
              l++;
              left = left.getParent();
            }
            while (right.getParent() != null) {
              r++;
              right = right.getParent();
            }
            return l - r;
          }
        };

    Collections.sort(ordered_deps, comparator);
    this.setDependencies(ordered_deps);
  }

  /**
   * <p>Getter for the field <code>createdAt</code>.</p>
   *
   * @return a {@link java.util.Calendar} object.
   */
  public java.util.Calendar getCreatedAt() {
    return createdAt;
  }
  /**
   * <p>Setter for the field <code>createdAt</code>.</p>
   *
   * @param createdAt a {@link java.util.Calendar} object.
   */
  public void setCreatedAt(java.util.Calendar createdAt) {
    this.createdAt = createdAt;
  }

  /**
   * <p>Getter for the field <code>modifiedAt</code>.</p>
   *
   * @return a {@link java.util.Calendar} object.
   */
  public java.util.Calendar getModifiedAt() {
    return modifiedAt;
  }
  /**
   * <p>Setter for the field <code>modifiedAt</code>.</p>
   *
   * @param modifiedAt a {@link java.util.Calendar} object.
   */
  public void setModifiedAt(java.util.Calendar modifiedAt) {
    this.modifiedAt = modifiedAt;
  }

  /**
   * <p>Getter for the field <code>lastScan</code>.</p>
   *
   * @return a {@link java.util.Calendar} object.
   */
  public java.util.Calendar getLastScan() {
    return lastScan;
  }
  /**
   * <p>Setter for the field <code>lastScan</code>.</p>
   *
   * @param lastScan a {@link java.util.Calendar} object.
   */
  public void setLastScan(java.util.Calendar lastScan) {
    this.lastScan = lastScan;
  }

  /**
   * <p>Getter for the field <code>lastVulnChange</code>.</p>
   *
   * @return a {@link java.util.Calendar} object.
   */
  public java.util.Calendar getLastVulnChange() {
    return lastVulnChange;
  }
  /**
   * <p>Setter for the field <code>lastVulnChange</code>.</p>
   *
   * @param lastVulnChange a {@link java.util.Calendar} object.
   */
  public void setLastVulnChange(java.util.Calendar lastVulnChange) {
    this.lastVulnChange = lastVulnChange;
  }

  /**
   * <p>getLastChange.</p>
   *
   * @return a {@link java.util.Calendar} object.
   */
  @JsonProperty(value = "lastChange")
  public java.util.Calendar getLastChange() {
    if (this.getLastVulnChange() == null) return this.getLastScan();
    else if (this.getLastScan() == null) return this.getLastVulnChange();
    else
      return (this.getLastVulnChange().after(this.getLastScan())
          ? this.getLastVulnChange()
          : this.getLastScan());
  }

  /**
   * Removes all application {@link ConstructId}s and {@link Dependency}s.
   */
  public void clean() {
    this.setConstructs(new HashSet<ConstructId>());
    this.setDependencies(new HashSet<Dependency>());
  }

  /**
   * <p>countConstructs.</p>
   *
   * @return a int.
   */
  @JsonProperty(value = "constructCounter")
  @JsonView(Views.CountDetails.class)
  public int countConstructs() {
    return (this.getConstructs() == null ? 0 : this.getConstructs().size());
  }

  /**
   * <p>countConstructTypes.</p>
   *
   * @return a {@link org.eclipse.steady.backend.model.ConstructIdFilter} object.
   */
  @JsonProperty(value = "constructTypeCounters")
  @JsonView(Views.CountDetails.class)
  public ConstructIdFilter countConstructTypes() {
    return new ConstructIdFilter(this.getConstructs());
  }

  /**
   * <p>countDependencies.</p>
   *
   * @return a int.
   */
  @JsonProperty(value = "countDependencies")
  @JsonView(Views.CountDetails.class)
  public int countDependencies() {
    return (this.getDependencies() == null ? -1 : this.getDependencies().size());
  }

  /**
   * <p>countConstructTypesPerPackage.</p>
   *
   * @return a {@link org.eclipse.steady.backend.model.PackageStatistics} object.
   */
  @JsonProperty(value = "packageCounters")
  @JsonView(Views.CountDetails.class)
  public PackageStatistics countConstructTypesPerPackage() {
    if (this.packageStats == null) this.packageStats = new PackageStatistics(this.getConstructs());
    return this.packageStats;
  }

  /**
   * <p>countTracedConstructTypesPerPackage.</p>
   *
   * @return a {@link org.eclipse.steady.backend.model.PackageStatistics} object.
   */
  @JsonProperty(value = "packageTraceCounters")
  @JsonView(Views.CountDetails.class)
  public PackageStatistics countTracedConstructTypesPerPackage() {
    if (this.tracedPackageStats == null && this.getTraces() != null) {
      final Set<ConstructId> cids = new HashSet<ConstructId>();
      for (Trace t : this.getTraces()) {
        final ConstructId cid = t.getConstructId();
        final ConstructId pid = ConstructId.getPackageOf(cid);
        // Avoid adding up traces of test classes, which are typically in the same Java package than
        // the
        // tested app classes, and which can lead to coverages > 100%
        if (this.getConstructs().contains(pid)) {
          if (this.getConstructs().contains(cid)) cids.add(t.getConstructId());
        } else {
          cids.add(t.getConstructId());
        }
      }
      this.tracedPackageStats = new PackageStatistics(cids);
    }
    return this.tracedPackageStats;
  }

  /**
   * <p>prePersist.</p>
   */
  @PrePersist
  public void prePersist() {
    if (this.getCreatedAt() == null) {
      this.setCreatedAt(Calendar.getInstance());
    }
    this.setModifiedAt(Calendar.getInstance());
    this.setLastScan(Calendar.getInstance());
    this.setLastVulnChange(Calendar.getInstance());
  }

  //	@PreUpdate
  //	public void preUpdate() {
  //		this.setModifiedAt(Calendar.getInstance());
  //	}

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((artifact == null) ? 0 : artifact.hashCode());
    result = prime * result + ((mvnGroup == null) ? 0 : mvnGroup.hashCode());
    result = prime * result + ((space == null) ? 0 : space.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Application other = (Application) obj;
    if (artifact == null) {
      if (other.artifact != null) return false;
    } else if (!artifact.equals(other.artifact)) return false;
    if (mvnGroup == null) {
      if (other.mvnGroup != null) return false;
    } else if (!mvnGroup.equals(other.mvnGroup)) return false;
    if (space == null) {
      if (other.space != null) return false;
    } else if (!space.equals(other.space)) return false;
    if (version == null) {
      if (other.version != null) return false;
    } else if (!version.equals(other.version)) return false;
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * Compares using an {@link Application}'s space, group, artifact and version.
   */
  @Override
  public int compareTo(Object _other) {
    if (_other == null || !(_other instanceof Application)) throw new IllegalArgumentException();
    int v =
        this.getSpace()
            .getSpaceToken()
            .compareTo(((Application) _other).getSpace().getSpaceToken());
    if (v == 0) v = this.getMvnGroup().compareTo(((Application) _other).getMvnGroup());
    if (v == 0) v = this.getArtifact().compareTo(((Application) _other).getArtifact());
    if (v == 0) v = this.getVersion().compareTo(((Application) _other).getVersion());
    return v;
  }

  /** {@inheritDoc} */
  @Override
  public final String toString() {
    return this.toString(false);
  }

  /**
   * <p>toString.</p>
   *
   * @param _deep a boolean.
   * @return a {@link java.lang.String} object.
   */
  public final String toString(boolean _deep) {
    final StringBuilder builder = new StringBuilder();
    if (_deep) {
      builder
          .append("Application ")
          .append(this.toString(false))
          .append(System.getProperty("line.separator"));
      for (ConstructId cid : this.getConstructs()) {
        builder
            .append("    ConstructId     ")
            .append(cid)
            .append(System.getProperty("line.separator"));
      }
    } else {
      builder.append("[").append(this.getSpace().getSpaceName()).append(":");
      builder
          .append(this.getMvnGroup())
          .append(":")
          .append(this.getArtifact())
          .append(":")
          .append(this.getVersion())
          .append("]");
    }
    return builder.toString();
  }

  /**
   * <p>equalsIgnoreSpace.</p>
   *
   * @param obj a {@link java.lang.Object} object.
   * @return a boolean.
   */
  public boolean equalsIgnoreSpace(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Application other = (Application) obj;
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
}

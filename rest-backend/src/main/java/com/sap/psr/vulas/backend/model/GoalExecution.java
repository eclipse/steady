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
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.sap.psr.vulas.backend.model.view.Views;
import com.sap.psr.vulas.shared.enums.GoalType;

/**
 * <p>GoalExecution class.</p>
 *
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(
    ignoreUnknown = true,
    value = {"createdAt"},
    allowGetters = true)
@Entity
@Table(
    name = "AppGoalExe",
    uniqueConstraints = {
      @UniqueConstraint(columnNames = {"app", "goal", "startedAtClient"}),
      @UniqueConstraint(columnNames = {"executionId"})
    })
public class GoalExecution implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "app", referencedColumnName = "id")
  @JsonBackReference // Required in order to omit the app property when de-serializing JSON
  private Application app;

  @Column(nullable = false, length = 9)
  @Enumerated(EnumType.STRING)
  private GoalType goal;

  /**
   * When the entry was created in the backend.
   */
  @Temporal(TemporalType.TIMESTAMP)
  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
      timezone = "GMT")
  private java.util.Calendar createdAt;

  /**
   * When the goal was started on client side.
   */
  @Temporal(TemporalType.TIMESTAMP)
  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
      timezone = "GMT")
  private java.util.Calendar startedAtClient;

  @Column private long runtimeNano;

  /**
   * Client-generated ID.
   */
  @Column private String executionId;

  /**
   * Exception that occurred on client side (if any).
   */
  @Column(columnDefinition = "text")
  @JsonProperty("exception")
  private String exception;

  @Column private long memMax;

  @Column private long memUsedMax;

  @Column private long memUsedAvg;

  @Column private String clientVersion;

  @ManyToMany(
      cascade = {},
      fetch = FetchType.LAZY)
  @JsonView(Views.GoalDetails.class)
  private Collection<Property> configuration;

  // Cache
  @Transient private Map<String, String> configurationMap = null;

  @ManyToMany(
      cascade = {},
      fetch = FetchType.LAZY)
  @JsonView(Views.GoalDetails.class)
  private Collection<Property> systemInfo;

  // Cache
  @Transient private Map<String, String> systemInfoMap = null;

  @ElementCollection
  @CollectionTable(name = "AppGoalExeStatistics")
  @JsonView(Views.GoalDetails.class)
  private Map<String, Long> statistics;

  /**
   * <p>Constructor for GoalExecution.</p>
   */
  public GoalExecution() {
    super();
  }

  /**
   * <p>Constructor for GoalExecution.</p>
   *
   * @param app a {@link com.sap.psr.vulas.backend.model.Application} object.
   * @param goal a {@link com.sap.psr.vulas.shared.enums.GoalType} object.
   * @param startedAtClient a {@link java.util.Calendar} object.
   */
  public GoalExecution(Application app, GoalType goal, Calendar startedAtClient) {
    super();
    this.app = app;
    this.goal = goal;
    this.startedAtClient = startedAtClient;
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
   * <p>Getter for the field <code>app</code>.</p>
   *
   * @return a {@link com.sap.psr.vulas.backend.model.Application} object.
   */
  public Application getApp() {
    return app;
  }
  /**
   * <p>Setter for the field <code>app</code>.</p>
   *
   * @param app a {@link com.sap.psr.vulas.backend.model.Application} object.
   */
  public void setApp(Application app) {
    this.app = app;
  }

  /**
   * <p>Getter for the field <code>goal</code>.</p>
   *
   * @return a {@link com.sap.psr.vulas.shared.enums.GoalType} object.
   */
  public GoalType getGoal() {
    return goal;
  }
  /**
   * <p>Setter for the field <code>goal</code>.</p>
   *
   * @param goal a {@link com.sap.psr.vulas.shared.enums.GoalType} object.
   */
  public void setGoal(GoalType goal) {
    this.goal = goal;
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
   * <p>Getter for the field <code>startedAtClient</code>.</p>
   *
   * @return a {@link java.util.Calendar} object.
   */
  public java.util.Calendar getStartedAtClient() {
    return startedAtClient;
  }
  /**
   * <p>Setter for the field <code>startedAtClient</code>.</p>
   *
   * @param startedAtClient a {@link java.util.Calendar} object.
   */
  public void setStartedAtClient(java.util.Calendar startedAtClient) {
    this.startedAtClient = startedAtClient;
  }

  /**
   * <p>Getter for the field <code>runtimeNano</code>.</p>
   *
   * @return a long.
   */
  public long getRuntimeNano() {
    return runtimeNano;
  }
  /**
   * <p>Setter for the field <code>runtimeNano</code>.</p>
   *
   * @param runtimeNano a long.
   */
  public void setRuntimeNano(long runtimeNano) {
    this.runtimeNano = runtimeNano;
  }

  /**
   * <p>Getter for the field <code>executionId</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getExecutionId() {
    return executionId;
  }
  /**
   * <p>Setter for the field <code>executionId</code>.</p>
   *
   * @param _id a {@link java.lang.String} object.
   */
  public void setExecutionId(String _id) {
    this.executionId = _id;
  }

  /**
   * <p>getExecutionException.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getExecutionException() {
    return exception;
  }
  /**
   * <p>setExecutionException.</p>
   *
   * @param exception a {@link java.lang.String} object.
   */
  public void setExecutionException(String exception) {
    this.exception = exception;
  }

  /**
   * <p>Getter for the field <code>memMax</code>.</p>
   *
   * @return a long.
   */
  public long getMemMax() {
    return memMax;
  }
  /**
   * <p>Setter for the field <code>memMax</code>.</p>
   *
   * @param memMax a long.
   */
  public void setMemMax(long memMax) {
    this.memMax = memMax;
  }

  /**
   * <p>Getter for the field <code>memUsedMax</code>.</p>
   *
   * @return a long.
   */
  public long getMemUsedMax() {
    return memUsedMax;
  }
  /**
   * <p>Setter for the field <code>memUsedMax</code>.</p>
   *
   * @param memUsedMax a long.
   */
  public void setMemUsedMax(long memUsedMax) {
    this.memUsedMax = memUsedMax;
  }

  /**
   * <p>Getter for the field <code>memUsedAvg</code>.</p>
   *
   * @return a long.
   */
  public long getMemUsedAvg() {
    return memUsedAvg;
  }
  /**
   * <p>Setter for the field <code>memUsedAvg</code>.</p>
   *
   * @param memUsedAvg a long.
   */
  public void setMemUsedAvg(long memUsedAvg) {
    this.memUsedAvg = memUsedAvg;
  }

  /**
   * <p>Getter for the field <code>clientVersion</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getClientVersion() {
    return clientVersion;
  }
  /**
   * <p>Setter for the field <code>clientVersion</code>.</p>
   *
   * @param clientVersion a {@link java.lang.String} object.
   */
  public void setClientVersion(String clientVersion) {
    this.clientVersion = clientVersion;
  }

  /**
   * <p>Getter for the field <code>configuration</code>.</p>
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<Property> getConfiguration() {
    return configuration;
  }

  /**
   * <p>Setter for the field <code>configuration</code>.</p>
   *
   * @param configuration a {@link java.util.Collection} object.
   */
  public void setConfiguration(Collection<Property> configuration) {
    this.configuration = configuration;
  }

  /**
   * Returns the value of the configuration property with the given name (or null, if no such property exists).
   *
   * @param _name a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  @JsonIgnore
  public String getConfiguration(@NotNull String _name) {
    if (this.configurationMap == null) {
      this.configurationMap = new HashMap<String, String>();
      for (Property p : this.getConfiguration())
        this.configurationMap.put(p.getName(), p.getPropertyValue());
    }
    return this.configurationMap.get(_name);
  }

  /**
   * Returns all configuration properties.
   *
   * @return a {@link java.util.HashMap} object.
   */
  @JsonIgnore
  public Map<String, String> getConfigurationMap() {
    if (this.configurationMap == null) {
      this.configurationMap = new HashMap<String, String>();
      for (Property p : this.getConfiguration())
        this.configurationMap.put(p.getName(), p.getPropertyValue());
    }
    return this.configurationMap;
  }

  /**
   * <p>Getter for the field <code>systemInfo</code>.</p>
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<Property> getSystemInfo() {
    return systemInfo;
  }

  /**
   * <p>Setter for the field <code>systemInfo</code>.</p>
   *
   * @param systemInfo a {@link java.util.Collection} object.
   */
  public void setSystemInfo(Collection<Property> systemInfo) {
    this.systemInfo = systemInfo;
  }

  /**
   * Returns the value of the system info with the given name (or null, if no such property exists).
   *
   * @param _name a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  @JsonIgnore
  public String getSystemInfo(@NotNull String _name) {
    if (this.systemInfoMap == null) {
      this.systemInfoMap = new HashMap<String, String>();
      for (Property p : this.getSystemInfo())
        this.systemInfoMap.put(p.getName(), p.getPropertyValue());
    }
    return this.systemInfoMap.get(_name);
  }

  /**
   * <p>Getter for the field <code>statistics</code>.</p>
   *
   * @return a {@link java.util.Map} object.
   */
  public Map<String, Long> getStatistics() {
    return statistics;
  }
  /**
   * <p>Setter for the field <code>statistics</code>.</p>
   *
   * @param statistics a {@link java.util.Map} object.
   */
  public void setStatistics(Map<String, Long> statistics) {
    this.statistics = statistics;
  }

  /**
   * Sets {@link GoalExecution#createdAt}.
   */
  @PrePersist
  public void prePersist() {
    if (this.getCreatedAt() == null) {
      this.setCreatedAt(Calendar.getInstance());
    }
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((app == null) ? 0 : app.hashCode());
    result = prime * result + ((executionId == null) ? 0 : executionId.hashCode());
    result = prime * result + ((goal == null) ? 0 : goal.hashCode());
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    GoalExecution other = (GoalExecution) obj;
    if (app == null) {
      if (other.app != null) return false;
    } else if (!app.equals(other.app)) return false;
    if (executionId == null) {
      if (other.executionId != null) return false;
    } else if (!executionId.equals(other.executionId)) return false;
    if (goal != other.goal) return false;
    return true;
  }

  /*@Override
  public int hashCode() {
  	final int prime = 31;
  	int result = 1;
  	result = prime * result + ((app == null) ? 0 : app.hashCode());
  	result = prime * result + ((goal == null) ? 0 : goal.hashCode());
  	result = prime * result + ((startedAtClient == null) ? 0 : startedAtClient.hashCode());
  	return result;
  }

  @Override
  public boolean equals(Object obj) {
  	if (this == obj)
  		return true;
  	if (obj == null)
  		return false;
  	if (getClass() != obj.getClass())
  		return false;
  	GoalExecution other = (GoalExecution) obj;
  	if (app == null) {
  		if (other.app != null)
  			return false;
  	} else if (!app.equals(other.app))
  		return false;
  	if (goal != other.goal)
  		return false;
  	if (startedAtClient == null) {
  		if (other.startedAtClient != null)
  			return false;
  	} else if (!startedAtClient.equals(other.startedAtClient))
  		return false;
  	return true;
  }*/

}

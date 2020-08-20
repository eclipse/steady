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

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * <p>Trace class.</p>
 *
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(
    ignoreUnknown = true,
    value = {"filename"},
    allowSetters = true)
public class Trace implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonIgnore private Long id;

  @JsonBackReference // Required in order to omit the app property when de-serializing JSON
  private Application app;

  private Library lib;

  private String filename;

  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
      timezone = "GMT")
  private java.util.Calendar tracedAt;

  private ConstructId constructId;

  /**
   * ID of the TEST GoalExecution during which the trace was collected.
   * The member is of type {@link String} rather than GoalExecution, as the
   * latter will only be uploaded at the very end of the goal execution, hence, a foreign
   * key relationship could not be satisfied.
   */
  private String executionId;

  private int count;

  /**
   * <p>Constructor for Trace.</p>
   */
  public Trace() {
    super();
  }

  /**
   * <p>Constructor for Trace.</p>
   *
   * @param app a {@link com.sap.psr.vulas.shared.json.model.Application} object.
   * @param lib a {@link com.sap.psr.vulas.shared.json.model.Library} object.
   * @param constructId a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   */
  public Trace(Application app, Library lib, ConstructId constructId) {
    super();
    this.app = app;
    this.lib = lib;
    this.constructId = constructId;
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
   * @return a {@link com.sap.psr.vulas.shared.json.model.Application} object.
   */
  public Application getApp() {
    return app;
  }
  /**
   * <p>Setter for the field <code>app</code>.</p>
   *
   * @param app a {@link com.sap.psr.vulas.shared.json.model.Application} object.
   */
  public void setApp(Application app) {
    this.app = app;
  }

  /**
   * <p>Getter for the field <code>lib</code>.</p>
   *
   * @return a {@link com.sap.psr.vulas.shared.json.model.Library} object.
   */
  public Library getLib() {
    return lib;
  }
  /**
   * <p>Setter for the field <code>lib</code>.</p>
   *
   * @param lib a {@link com.sap.psr.vulas.shared.json.model.Library} object.
   */
  public void setLib(Library lib) {
    this.lib = lib;
  }

  /**
   * <p>Getter for the field <code>tracedAt</code>.</p>
   *
   * @return a {@link java.util.Calendar} object.
   */
  public java.util.Calendar getTracedAt() {
    return tracedAt;
  }
  /**
   * <p>Setter for the field <code>tracedAt</code>.</p>
   *
   * @param tracedAt a {@link java.util.Calendar} object.
   */
  public void setTracedAt(java.util.Calendar tracedAt) {
    this.tracedAt = tracedAt;
  }

  /**
   * <p>Getter for the field <code>constructId</code>.</p>
   *
   * @return a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   */
  public ConstructId getConstructId() {
    return constructId;
  }
  /**
   * <p>Setter for the field <code>constructId</code>.</p>
   *
   * @param constructId a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   */
  public void setConstructId(ConstructId constructId) {
    this.constructId = constructId;
  }

  /**
   * <p>Getter for the field <code>count</code>.</p>
   *
   * @return a int.
   */
  public int getCount() {
    return count;
  }
  /**
   * <p>Setter for the field <code>count</code>.</p>
   *
   * @param count a int.
   */
  public void setCount(int count) {
    this.count = count;
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
   * @param executionId a {@link java.lang.String} object.
   */
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
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

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((app == null) ? 0 : app.hashCode());
    result = prime * result + ((constructId == null) ? 0 : constructId.hashCode());
    result = prime * result + ((lib == null) ? 0 : lib.hashCode());
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Trace other = (Trace) obj;
    if (app == null) {
      if (other.app != null) return false;
    } else if (!app.equals(other.app)) return false;
    if (constructId == null) {
      if (other.constructId != null) return false;
    } else if (!constructId.equals(other.constructId)) return false;
    if (lib == null) {
      if (other.lib != null) return false;
    } else if (!lib.equals(other.lib)) return false;
    return true;
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
          .append("Trace")
          .append(this.toString(false))
          .append(System.getProperty("line.separator"));
      builder.append("    app ").append(this.getApp()).append(System.getProperty("line.separator"));
      builder.append("    lib ").append(this.getLib()).append(System.getProperty("line.separator"));
    } else {
      builder
          .append("[")
          .append(this.getId())
          .append(":")
          .append(this.getConstructId().getQname())
          .append("]");
    }
    return builder.toString();
  }
}

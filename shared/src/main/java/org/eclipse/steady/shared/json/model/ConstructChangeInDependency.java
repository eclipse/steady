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
package org.eclipse.steady.shared.json.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * <p>ConstructChangeInDependency class.</p>
 *
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = false)
public class ConstructChangeInDependency implements Serializable {

  private Trace trace;

  private Boolean traced;
  private Boolean reachable;
  private Boolean inArchive;
  private Boolean affected;
  private Boolean classInArchive;
  private Boolean equalChangeType;
  private String overall_change;

  private ConstructChange constructChange;

  /**
   * <p>Constructor for ConstructChangeInDependency.</p>
   */
  public ConstructChangeInDependency() {
    super();
  }

  /**
   * <p>Getter for the field <code>constructChange</code>.</p>
   *
   * @return a {@link org.eclipse.steady.shared.json.model.ConstructChange} object.
   */
  public ConstructChange getConstructChange() {
    return constructChange;
  }
  /**
   * <p>Setter for the field <code>constructChange</code>.</p>
   *
   * @param constructChange a {@link org.eclipse.steady.shared.json.model.ConstructChange} object.
   */
  public void setConstructChange(ConstructChange constructChange) {
    this.constructChange = constructChange;
  }

  /**
   * <p>Getter for the field <code>trace</code>.</p>
   *
   * @return a {@link org.eclipse.steady.shared.json.model.Trace} object.
   */
  public Trace getTrace() {
    return trace;
  }
  /**
   * <p>Setter for the field <code>trace</code>.</p>
   *
   * @param trace a {@link org.eclipse.steady.shared.json.model.Trace} object.
   */
  public void setTrace(Trace trace) {
    this.trace = trace;
  }

  /**
   * <p>Getter for the field <code>traced</code>.</p>
   *
   * @return a {@link java.lang.Boolean} object.
   */
  public Boolean getTraced() {
    return traced;
  }
  /**
   * <p>Setter for the field <code>traced</code>.</p>
   *
   * @param traced a {@link java.lang.Boolean} object.
   */
  public void setTraced(Boolean traced) {
    this.traced = traced;
  }

  /**
   * <p>Getter for the field <code>inArchive</code>.</p>
   *
   * @return a {@link java.lang.Boolean} object.
   */
  public Boolean getInArchive() {
    return inArchive;
  }
  /**
   * <p>Setter for the field <code>inArchive</code>.</p>
   *
   * @param inArchive a {@link java.lang.Boolean} object.
   */
  public void setInArchive(Boolean inArchive) {
    this.inArchive = inArchive;
  }

  /**
   * <p>Getter for the field <code>reachable</code>.</p>
   *
   * @return a {@link java.lang.Boolean} object.
   */
  public Boolean getReachable() {
    return reachable;
  }
  /**
   * <p>Setter for the field <code>reachable</code>.</p>
   *
   * @param reachable a {@link java.lang.Boolean} object.
   */
  public void setReachable(Boolean reachable) {
    this.reachable = reachable;
  }

  /**
   * <p>isAffected.</p>
   *
   * @return a boolean.
   */
  public boolean isAffected() {
    return affected;
  }

  /**
   * <p>Setter for the field <code>affected</code>.</p>
   *
   * @param affected a boolean.
   */
  public void setAffected(boolean affected) {
    this.affected = affected;
  }

  /**
   * <p>isClassInArchive.</p>
   *
   * @return a boolean.
   */
  public boolean isClassInArchive() {
    return classInArchive;
  }

  /**
   * <p>Setter for the field <code>classInArchive</code>.</p>
   *
   * @param classInArchive a boolean.
   */
  public void setClassInArchive(boolean classInArchive) {
    this.classInArchive = classInArchive;
  }

  /**
   * <p>isEqualChangeType.</p>
   *
   * @return a boolean.
   */
  public boolean isEqualChangeType() {
    return equalChangeType;
  }

  /**
   * <p>Setter for the field <code>equalChangeType</code>.</p>
   *
   * @param equalChangeType a boolean.
   */
  public void setEqualChangeType(boolean equalChangeType) {
    this.equalChangeType = equalChangeType;
  }

  /**
   * <p>Getter for the field <code>overall_change</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getOverall_change() {
    return overall_change;
  }

  /**
   * <p>Setter for the field <code>overall_change</code>.</p>
   *
   * @param overall_change a {@link java.lang.String} object.
   */
  public void setOverall_change(String overall_change) {
    this.overall_change = overall_change;
  }
}

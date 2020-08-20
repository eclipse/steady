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

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sap.psr.vulas.shared.enums.ConstructType;

/**
 * Set of {@link ConstructId}s of type {@link ConstructType#CLAS} that exist in two {@link Dependency}s of
 * an {@link Application}.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DependencyIntersection {

  private Dependency d1 = null;

  private Dependency d2 = null;

  private Collection<ConstructId> constructs = null;

  /**
   * <p>Constructor for DependencyIntersection.</p>
   *
   * @param _d1 a {@link com.sap.psr.vulas.backend.model.Dependency} object.
   * @param _d2 a {@link com.sap.psr.vulas.backend.model.Dependency} object.
   * @param _constructs a long.
   */
  public DependencyIntersection(Dependency _d1, Dependency _d2, long _constructs) {}

  /**
   * <p>Getter for the field <code>d1</code>.</p>
   *
   * @return a {@link com.sap.psr.vulas.backend.model.Dependency} object.
   */
  public Dependency getD1() {
    return d1;
  }

  /**
   * <p>Setter for the field <code>d1</code>.</p>
   *
   * @param d1 a {@link com.sap.psr.vulas.backend.model.Dependency} object.
   */
  public void setD1(Dependency d1) {
    this.d1 = d1;
  }

  /**
   * <p>Getter for the field <code>d2</code>.</p>
   *
   * @return a {@link com.sap.psr.vulas.backend.model.Dependency} object.
   */
  public Dependency getD2() {
    return d2;
  }

  /**
   * <p>Setter for the field <code>d2</code>.</p>
   *
   * @param d2 a {@link com.sap.psr.vulas.backend.model.Dependency} object.
   */
  public void setD2(Dependency d2) {
    this.d2 = d2;
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
}

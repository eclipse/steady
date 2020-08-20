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

/**
 * <p>PathNode class.</p>
 *
 */
public class PathNode implements Serializable {

  private ConstructId constructId;

  private Library lib;

  /**
   * <p>Constructor for PathNode.</p>
   */
  public PathNode() {
    super();
  }

  /**
   * <p>Constructor for PathNode.</p>
   *
   * @param _cid a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   */
  public PathNode(ConstructId _cid) {
    this(_cid, null);
  }

  /**
   * <p>Constructor for PathNode.</p>
   *
   * @param _cid a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   * @param _lib a {@link com.sap.psr.vulas.shared.json.model.Library} object.
   */
  public PathNode(ConstructId _cid, Library _lib) {
    super();
    this.constructId = _cid;
    this.lib = _lib;
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

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
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
    PathNode other = (PathNode) obj;
    if (constructId == null) {
      if (other.constructId != null) return false;
    } else if (!constructId.equals(other.constructId)) return false;
    if (lib == null) {
      if (other.lib != null) return false;
    } else if (!lib.equals(other.lib)) return false;
    return true;
  }
}

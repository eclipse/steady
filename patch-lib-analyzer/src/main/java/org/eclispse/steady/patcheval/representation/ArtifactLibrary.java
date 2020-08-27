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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eclispse.steady.patcheval.representation;

/**
 * Helper  class for deserialization of all versions of artifacts returned from CIA, respect to a certain group,artifact.
 */
public class ArtifactLibrary {
  String g, a, v;
  Long timestamp;

  /**
   * <p>Constructor for ArtifactLibrary.</p>
   *
   * @param g a {@link java.lang.String} object.
   * @param a a {@link java.lang.String} object.
   * @param v a {@link java.lang.String} object.
   * @param timestamp a {@link java.lang.Long} object.
   */
  public ArtifactLibrary(String g, String a, String v, Long timestamp) {
    this.g = g;
    this.a = a;
    this.v = v;
    this.timestamp = timestamp;
  }

  /**
   * <p>Getter for the field <code>g</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getG() {
    return g;
  }

  /**
   * <p>Setter for the field <code>g</code>.</p>
   *
   * @param g a {@link java.lang.String} object.
   */
  public void setG(String g) {
    this.g = g;
  }

  /**
   * <p>Getter for the field <code>a</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getA() {
    return a;
  }

  /**
   * <p>Setter for the field <code>a</code>.</p>
   *
   * @param a a {@link java.lang.String} object.
   */
  public void setA(String a) {
    this.a = a;
  }

  /**
   * <p>Getter for the field <code>v</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getV() {
    return v;
  }

  /**
   * <p>Setter for the field <code>v</code>.</p>
   *
   * @param v a {@link java.lang.String} object.
   */
  public void setV(String v) {
    this.v = v;
  }

  /**
   * <p>Getter for the field <code>timestamp</code>.</p>
   *
   * @return a {@link java.lang.Long} object.
   */
  public Long getTimestamp() {
    return timestamp;
  }

  /**
   * <p>Setter for the field <code>timestamp</code>.</p>
   *
   * @param timestamp a {@link java.lang.Long} object.
   */
  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }
}

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
package com.sap.psr.vulas.cia.model.pypi;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * <p>PypiResponse class.</p>
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PypiResponse {

  PypiInfo info;

  LinkedHashMap<String, ArrayList<PypiRelease>> releases;

  /**
   * <p>Getter for the field <code>info</code>.</p>
   *
   * @return a {@link com.sap.psr.vulas.cia.model.pypi.PypiInfo} object.
   */
  public PypiInfo getInfo() {
    return info;
  }

  /**
   * <p>Setter for the field <code>info</code>.</p>
   *
   * @param info a {@link com.sap.psr.vulas.cia.model.pypi.PypiInfo} object.
   */
  public void setInfo(PypiInfo info) {
    this.info = info;
  }

  /**
   * <p>Getter for the field <code>releases</code>.</p>
   *
   * @return a {@link java.util.LinkedHashMap} object.
   */
  public LinkedHashMap<String, ArrayList<PypiRelease>> getReleases() {
    return releases;
  }

  /**
   * <p>Setter for the field <code>releases</code>.</p>
   *
   * @param releases a {@link java.util.LinkedHashMap} object.
   */
  public void setReleases(LinkedHashMap<String, ArrayList<PypiRelease>> releases) {
    this.releases = releases;
  }
}

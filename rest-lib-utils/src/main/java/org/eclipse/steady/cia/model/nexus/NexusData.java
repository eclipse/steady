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
 * SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
 */
package org.eclipse.steady.cia.model.nexus;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * <p>NexusData class.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NexusData {
  private String instance;

  private String repositoryId;

  private Collection<NexusLibId> gavs;

  /**
   * <p>Getter for the field <code>instance</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getInstance() {
    return instance;
  }

  /**
   * <p>Setter for the field <code>instance</code>.</p>
   *
   * @param instance a {@link java.lang.String} object.
   */
  public void setInstance(String instance) {
    this.instance = instance;
  }

  /**
   * <p>Getter for the field <code>repositoryId</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getRepositoryId() {
    return repositoryId;
  }

  /**
   * <p>Setter for the field <code>repositoryId</code>.</p>
   *
   * @param repositoryId a {@link java.lang.String} object.
   */
  public void setRepositoryId(String repositoryId) {
    this.repositoryId = repositoryId;
  }

  /**
   * <p>Getter for the field <code>gavs</code>.</p>
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<NexusLibId> getGavs() {
    return gavs;
  }

  /**
   * <p>Setter for the field <code>gavs</code>.</p>
   *
   * @param gavs a {@link java.util.Collection} object.
   */
  public void setGavs(Collection<NexusLibId> gavs) {
    this.gavs = gavs;
  }
}

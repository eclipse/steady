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
package org.eclipse.steady.tasks;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.eclipse.steady.goals.GoalConfigurationException;
import org.eclipse.steady.shared.enums.GoalClient;
import org.eclipse.steady.shared.json.model.Application;
import org.eclipse.steady.shared.json.model.Dependency;
import org.eclipse.steady.shared.util.VulasConfiguration;

/**
 * <p>Abstract AbstractTask class.</p>
 *
 */
public abstract class AbstractTask implements Task {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private GoalClient client = null;

  private Application application = null;

  private List<Path> searchPaths = null;

  private Map<Path, Dependency> knownDependencies = null;

  protected VulasConfiguration vulasConfiguration = null;

  // ====================  Setter methods used to passed general context information to the task

  /**
   * <p>getSearchPath.</p>
   *
   * @return a {@link java.util.List} object.
   */
  public final List<Path> getSearchPath() {
    return searchPaths;
  }

  /**
   * <p>hasSearchPath.</p>
   *
   * @return a boolean.
   */
  public final boolean hasSearchPath() {
    return this.searchPaths != null && !this.searchPaths.isEmpty();
  }

  /** {@inheritDoc} */
  @Override
  public final void setSearchPaths(List<Path> _paths) {
    this.searchPaths = _paths;
  }

  /**
   * Returns true if the {@link GoalClient} of this task is equal to the given client, false otherwise.
   *
   * @param _client a {@link org.eclipse.steady.shared.enums.GoalClient} object.
   * @return a boolean.
   */
  public final boolean isGoalClient(GoalClient _client) {
    return this.client == _client;
  }

  /**
   * <p>isOneOfGoalClients.</p>
   *
   * @param clients a {@link java.util.List} object.
   * @return a boolean.
   */
  public final boolean isOneOfGoalClients(List<GoalClient> clients) {
    return clients.contains(this.client);
  }

  /** {@inheritDoc} */
  @Override
  public final void setGoalClient(GoalClient _client) {
    this.client = _client;
  }

  /**
   * <p>Getter for the field <code>application</code>.</p>
   *
   * @return a {@link org.eclipse.steady.shared.json.model.Application} object.
   */
  public final Application getApplication() {
    return this.application;
  }

  /** {@inheritDoc} */
  @Override
  public final void setApplication(Application _app) {
    this.application = _app;
  }

  /**
   * <p>Getter for the field <code>knownDependencies</code>.</p>
   *
   * @return a {@link java.util.Map} object.
   */
  public Map<Path, Dependency> getKnownDependencies() {
    return this.knownDependencies;
  }

  /** {@inheritDoc} */
  @Override
  public void setKnownDependencies(Map<Path, Dependency> _dependencies) {
    this.knownDependencies = _dependencies;
  }

  // ==================== Configure, execute and clean up

  /**
   * {@inheritDoc}
   *
   * Checks the search {@link Path} and {@link GoalClient}.
   */
  @Override
  public void configure(VulasConfiguration _cfg) throws GoalConfigurationException {
    this.vulasConfiguration = _cfg;
    if (!this.hasSearchPath()) log.warn("Task " + this + ": No search path specified");
    if (this.client == null) log.warn("Task " + this + ": No goal client specified");
  }

  /** {@inheritDoc} */
  @Override
  public void cleanUp() {}
}

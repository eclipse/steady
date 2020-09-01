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
package org.eclipse.steady.backend.repo;

import org.eclipse.steady.backend.model.Application;
import org.eclipse.steady.backend.model.GoalExecution;
import org.eclipse.steady.shared.enums.GoalType;

/**
 * <p>GoalExecutionRepositoryCustom interface.</p>
 *
 */
public interface GoalExecutionRepositoryCustom {

  /**
   * Updates the identifiers of independent JPE entities and saves the given {@link Application}.
   *
   * @param _app a {@link org.eclipse.steady.backend.model.Application} object.
   * @param _goal_execution a {@link org.eclipse.steady.backend.model.GoalExecution} object.
   * @return a {@link org.eclipse.steady.backend.model.GoalExecution} object.
   */
  public GoalExecution customSave(Application _app, GoalExecution _goal_execution);

  /**
   * Returns the latest {@link GoalExecution} for the given application, or null if no executions exist.
   *
   * @param _app a {@link org.eclipse.steady.backend.model.Application} object.
   * @param _type TODO
   * @return a {@link org.eclipse.steady.backend.model.GoalExecution} object.
   */
  public GoalExecution findLatestGoalExecution(Application _app, GoalType _type);
}

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
package org.eclipse.steady.backend.repo;

import java.util.Set;

import org.eclipse.steady.backend.model.ConstructId;
import org.eclipse.steady.backend.model.Dependency;
import org.eclipse.steady.backend.model.TouchPoint;

/**
 * Specifies additional methods of the {@link PathRepository}.
 */
public interface DependencyRepositoryCustom {

  /**
   * <p>saveReachableConstructIds.</p>
   *
   * @param _dep a {@link org.eclipse.steady.backend.model.Dependency} object.
   * @param _construct_ids an array of {@link org.eclipse.steady.backend.model.ConstructId} objects.
   * @return a {@link java.util.Set} object.
   */
  public Set<ConstructId> saveReachableConstructIds(Dependency _dep, ConstructId[] _construct_ids);

  /**
   * <p>saveTouchPoints.</p>
   *
   * @param _dep a {@link org.eclipse.steady.backend.model.Dependency} object.
   * @param _touch_points an array of {@link org.eclipse.steady.backend.model.TouchPoint} objects.
   * @return a {@link java.util.Set} object.
   */
  public Set<TouchPoint> saveTouchPoints(Dependency _dep, TouchPoint[] _touch_points);
}

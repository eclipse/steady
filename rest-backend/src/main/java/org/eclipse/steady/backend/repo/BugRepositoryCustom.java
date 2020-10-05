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

import javax.persistence.PersistenceException;

import org.eclipse.steady.backend.model.Bug;
import org.eclipse.steady.backend.model.ConstructId;
import org.springframework.data.repository.CrudRepository;

/**
 * Specifies additional methods of the {@link BugRepository}.
 */
public interface BugRepositoryCustom {

  /**
   * Saves the given {@link Bug} together with all the nested {@link ConstructId}s.
   * This method has to be used in favor of the save method provided by the
   * {@link CrudRepository}.
   *
   * @param bug a {@link org.eclipse.steady.backend.model.Bug} object.
   * @param _considerCC a {@link java.lang.Boolean} object.
   * @return a {@link org.eclipse.steady.backend.model.Bug} object.
   * @throws javax.persistence.PersistenceException if any.
   */
  public Bug customSave(Bug bug, Boolean _considerCC) throws PersistenceException;

  /**
   * Checks whether the given {@link Bug} needs CVE data and, if yes, updates its description and CVSS information.
   *
   * @return true if the bug was updated, false otherwise
   * @param _b a {@link org.eclipse.steady.backend.model.Bug} object.
   * @param _force a boolean.
   */
  public boolean updateCachedCveData(Bug _b, boolean _force);
}

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
package org.eclipse.steady.backend.util;

import java.util.Collection;

import javax.persistence.EntityNotFoundException;
import javax.validation.constraints.NotNull;

import org.springframework.data.repository.CrudRepository;

/**
 * {@link CrudRepository#findOne(java.io.Serializable)} only works for the primary key of the respective entity.
 * All other "find" methods that can be specified in the extended interface only return {@link Collection}s of objects
 * that match the search criteria. This class works around this problem...
 *
 * @param <T> type of objects handled by the filter.
 */
public class ResultSetFilter<T> {

  /**
   * <p>findOne.</p>
   *
   * @param _collection a {@link java.util.Collection} object.
   * @return the single object contained in the given collection.
   * @throws EntityNotFoundException if the given
   * collection is empty or contains multiple elements.
   */
  public T findOne(@NotNull Collection<T> _collection) throws EntityNotFoundException {
    if (_collection == null || _collection.isEmpty()) {
      throw new EntityNotFoundException("Object not found");
    } else if (_collection.size() > 1) {
      throw new EntityNotFoundException("Multiple objects found");
    } else {
      return _collection.iterator().next();
    }
  }
}

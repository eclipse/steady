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

import java.util.List;

import org.eclipse.steady.backend.model.Property;
import org.eclipse.steady.backend.util.ResultSetFilter;
import org.eclipse.steady.shared.enums.PropertySource;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * <p>PropertyRepository interface.</p>
 */
@Repository
public interface PropertyRepository extends CrudRepository<Property, Long> {

  /** Constant <code>FILTER</code> */
  public static final ResultSetFilter<Property> FILTER = new ResultSetFilter<Property>();

  /**
   * <p>findBySecondaryKey.</p>
   *
   * @param source a {@link org.eclipse.steady.shared.enums.PropertySource} object.
   * @param name a {@link java.lang.String} object.
   * @param value a {@link java.lang.String} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT prop FROM Property AS prop WHERE prop.source = :source AND prop.name = :name AND"
          + " prop.propertyValue = :value")
  List<Property> findBySecondaryKey(
      @Param("source") PropertySource source,
      @Param("name") String name,
      @Param("value") String value);
}

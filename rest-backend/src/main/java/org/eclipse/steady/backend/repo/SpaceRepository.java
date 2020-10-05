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

import org.eclipse.steady.backend.model.Space;
import org.eclipse.steady.backend.model.Tenant;
import org.eclipse.steady.backend.util.ResultSetFilter;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * <p>SpaceRepository interface.</p>
 *
 */
@Repository
public interface SpaceRepository extends CrudRepository<Space, Long>, SpaceRepositoryCustom {

  /** Constant <code>FILTER</code> */
  public static final ResultSetFilter<Space> FILTER = new ResultSetFilter<Space>();

  /**
   * All spaces of the given {@link Tenant}.
   *
   * @param tenant as String
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT s FROM Space AS s LEFT JOIN FETCH s.spaceOwners WHERE s.tenant.tenantToken = :tenant")
  List<Space> findAllTenantSpaces(@Param("tenant") String tenant);

  /**
   * All spaces of the given {@link Tenant} with public visibility.
   *
   * @param tenant as String
   * @param p a boolean.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT s FROM Space AS s LEFT JOIN FETCH s.spaceOwners WHERE s.tenant.tenantToken = :tenant"
          + " and s.isPublic = :p")
  List<Space> findAllTenantSpaces(@Param("tenant") String tenant, @Param("p") boolean p);

  /**
   * Should return just one space.
   *
   * @param token a {@link java.lang.String} object.
   * @return a {@link java.util.List} object.
   */
  @Query("SELECT s FROM Space AS s LEFT JOIN FETCH s.spaceOwners WHERE s.spaceToken = :token")
  List<Space> findBySecondaryKey(@Param("token") String token);

  /**
   * Should return just one space.
   *
   * @param tenant as String
   * @param token a {@link java.lang.String} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT s FROM Space AS s LEFT JOIN FETCH s.spaceOwners WHERE s.tenant = :tenant and"
          + " s.spaceToken = :token")
  List<Space> findBySecondaryKey(@Param("tenant") Tenant tenant, @Param("token") String token);

  /**
   * Should return just one space.
   *
   * @param tenant as String
   * @param token a {@link java.lang.String} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT s FROM Space AS s LEFT JOIN FETCH  s.tenant t WHERE t.tenantToken = :tenant and"
          + " s.spaceToken = :token")
  List<Space> findBySecondaryKey(@Param("tenant") String tenant, @Param("token") String token);

  /**
   * Should return just one space: the default space for the given tenant.
   *
   * @param tenant as String
   * @return a {@link org.eclipse.steady.backend.model.Space} object.
   */
  @Query(
      "SELECT s FROM Space AS s LEFT JOIN FETCH s.spaceOwners WHERE s.tenant.tenantToken = :tenant"
          + " and s.isDefault = true")
  Space findDefault(@Param("tenant") String tenant);
}

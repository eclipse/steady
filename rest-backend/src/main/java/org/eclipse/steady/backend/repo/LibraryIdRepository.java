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

import org.eclipse.steady.backend.model.Application;
import org.eclipse.steady.backend.model.LibraryId;
import org.eclipse.steady.backend.util.ResultSetFilter;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * <p>LibraryIdRepository interface.</p>
 */
@Repository
public interface LibraryIdRepository extends CrudRepository<LibraryId, Long> {

  /** Constant <code>FILTER</code> */
  public static final ResultSetFilter<LibraryId> FILTER = new ResultSetFilter<LibraryId>();

  /**
   * <p>findBySecondaryKey.</p>
   *
   * @param mvnGroup a {@link java.lang.String} object.
   * @param artifact a {@link java.lang.String} object.
   * @param version a {@link java.lang.String} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT libid FROM LibraryId AS libid WHERE libid.mvnGroup = :mvnGroup AND libid.artifact ="
          + " :artifact AND libid.version = :version")
  List<LibraryId> findBySecondaryKey(
      @Param("mvnGroup") String mvnGroup,
      @Param("artifact") String artifact,
      @Param("version") String version);

  /**
   * <p>findLibIds.</p>
   *
   * @param mvnGroup a {@link java.lang.String} object.
   * @param artifact a {@link java.lang.String} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT distinct libid FROM LibraryId AS libid JOIN FETCH libid.affLibraries  WHERE"
          + " libid.mvnGroup = :mvnGroup AND libid.artifact = :artifact")
  List<LibraryId> findLibIds(
      @Param("mvnGroup") String mvnGroup, @Param("artifact") String artifact);

  /**
   * <p>findArtifactIdsOSSI.</p>
   *
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT distinct libid.artifact FROM LibraryId AS libid where not libid.mvnGroup LIKE"
          + " 'com.sap%' and not libid.artifact LIKE 'com.sap%'")
  List<String> findArtifactIdsOSSI();

  /**
   * <p>findAllLibIdsOSSI.</p>
   *
   * @return a {@link java.util.List} object.
   */
  @Query("SELECT distinct libid FROM LibraryId AS libid where not libid.mvnGroup LIKE 'com.sap%'")
  List<LibraryId> findAllLibIdsOSSI();

  /**
   * <p>findBundledLibIdByApp.</p>
   *
   * @param app a {@link org.eclipse.steady.backend.model.Application} object
   * @return a {@link java.util.List} object
   */
  @Query(
      value =
          "select distinct d.id as dep_id, bl.bundled_library_ids_id as boundled_lid_id "
              + "   from app_dependency d "
              + "   inner join lib l1 on d.lib=l1.digest "
              + "   inner join lib_bundled_library_ids bl on l1.id=bl.library_id "
              + "   where d.app=:app and not l1.library_id_id = bl.bundled_library_ids_id  ",
      nativeQuery = true)
  List<Object[]> findBundledLibIdByApp(@Param("app") Application app);

  /**
   * <p>findBundledLibIdByGA.</p>
   *
   * @param mvnGroup a {@link java.lang.String} object
   * @param artifact a {@link java.lang.String} object
   * @return a {@link java.util.List} object
   */
  @Query(
      value =
          "select lid.id, bl.bundled_library_ids_id as boundled_lid_id    from library_id lid   "
              + " inner join lib l1 on lid.id=l1.library_id_id    inner join"
              + " lib_bundled_library_ids bl on l1.id=bl.library_id    where lid.mvn_group ="
              + " :mvnGroup AND lid.artifact = :artifact and not l1.library_id_id ="
              + " bl.bundled_library_ids_id  ",
      nativeQuery = true)
  List<Object[]> findBundledLibIdByGA(
      @Param("mvnGroup") String mvnGroup, @Param("artifact") String artifact);
}

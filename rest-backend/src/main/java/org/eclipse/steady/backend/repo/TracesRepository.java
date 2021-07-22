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

import javax.transaction.Transactional;

import org.eclipse.steady.backend.model.Application;
import org.eclipse.steady.backend.model.ConstructId;
import org.eclipse.steady.backend.model.Library;
import org.eclipse.steady.backend.model.Trace;
import org.eclipse.steady.backend.util.ResultSetFilter;
import org.eclipse.steady.shared.enums.GoalType;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * <p>TracesRepository interface.</p>
 */
@Repository
public interface TracesRepository extends CrudRepository<Trace, Long>, TracesRepositoryCustom {

  /** Constant <code>FILTER</code> */
  public static final ResultSetFilter<Trace> FILTER = new ResultSetFilter<Trace>();

  /**
   * <p>findByApp.</p>
   *
   * @param app a {@link org.eclipse.steady.backend.model.Application} object.
   * @return a {@link java.util.List} object.
   */
  @Query("SELECT DISTINCT t FROM Trace t " + " JOIN FETCH t.constructId " + " WHERE t.app = :app")
  List<Trace> findByApp(@Param("app") Application app);

  /**
   * Deletes all traces collected in the context of the given {@link Application}.
   * Called by goal {@link GoalType#CLEAN}.
   *
   * @param app a {@link org.eclipse.steady.backend.model.Application} object.
   */
  @Modifying
  @Transactional
  @Query("DELETE FROM Trace t WHERE t.app = :app")
  void deleteAllTracesForApp(@Param("app") Application app);

  /**
   * <p>findTracesOfAppConstruct.</p>
   *
   * @param app a {@link org.eclipse.steady.backend.model.Application} object.
   * @param constructId a {@link org.eclipse.steady.backend.model.ConstructId} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT t FROM Trace t WHERE t.app = :app AND t.lib IS NULL AND t.constructId = :constructId")
  List<Trace> findTracesOfAppConstruct(
      @Param("app") Application app, @Param("constructId") ConstructId constructId);

  /**
   * <p>findTracesOfLibraryConstruct.</p>
   *
   * @param app a {@link org.eclipse.steady.backend.model.Application} object.
   * @param lib a {@link org.eclipse.steady.backend.model.Library} object.
   * @param constructId a {@link org.eclipse.steady.backend.model.ConstructId} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT t FROM Trace t WHERE t.app = :app AND t.lib = :lib AND t.constructId = :constructId")
  List<Trace> findTracesOfLibraryConstruct(
      @Param("app") Application app,
      @Param("lib") Library lib,
      @Param("constructId") ConstructId constructId);

  /**
   * <p>findTracesOfLibrary.</p>
   *
   * @param app a {@link org.eclipse.steady.backend.model.Application} object.
   * @param lib a {@link org.eclipse.steady.backend.model.Library} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT DISTINCT t FROM Trace t "
          + " JOIN FETCH t.constructId "
          + " INNER JOIN t.lib"
          //   + " INNER JOIN t.lib l"
          //  + " LEFT OUTER JOIN FETCH l.libraryId "
          + " WHERE t.app = :app AND t.lib = :lib")
  //	@Query(value="SELECT * FROM APP_TRACE WHERE app =:app AND lib =:lib",nativeQuery=true)
  List<Trace> findTracesOfLibrary(@Param("app") Application app, @Param("lib") Library lib);

  /**
   * <p>countTracesOfExecConstructLibrary.</p>
   *
   * @param app a {@link org.eclipse.steady.backend.model.Application} object.
   * @param sha1 a {@link java.lang.String} object.
   * @return a {@link java.lang.Integer} object.
   */
  @Query(
      value =
          "SELECT COUNT(*) FROM "
              + " (SELECT DISTINCT app, construct_id ,lib FROM app_trace AS a"
              + "  WHERE app =:app AND lib =:lib "
              + "  ) as c  ",
      nativeQuery = true)
  Integer countTracesOfExecConstructLibrary(
      @Param("app") Application app, @Param("lib") String sha1);

  /**
   * <p>countTracesOfLibrary.</p>
   *
   * @param app a {@link org.eclipse.steady.backend.model.Application} object.
   * @param lib a {@link org.eclipse.steady.backend.model.Library} object.
   * @return a int.
   */
  @Query("SELECT count(t.id) FROM Trace AS t WHERE t.app = :app AND t.lib = :lib")
  int countTracesOfLibrary(@Param("app") Application app, @Param("lib") Library lib);

  //	@Query("SELECT"
  //			+ "   DISTINCT t FROM"
  //			+ "   Trace t"
  //			+ "	  JOIN FETCH t.constructId,"
  //			+ "   Bug b"
  //			+ "   JOIN b.constructChanges cc"
  //			+ " WHERE"
  //			+ "   cc.constructId = t.constructId"
  //			+ "   AND t.lib = :lib"
  //			+ "   AND t.app = :app"
  //			+ "   AND b = :bug")
  //	List<Trace> findVulnerableTracesOfLibraries(@Param("app") Application app, @Param("lib")
  // Library lib, @Param("bug") Bug bug);

  /**
   * <p>findVulnerableTracesOfLibraries.</p>
   *
   * @param app a {@link org.eclipse.steady.backend.model.Application} object.
   * @param lib a {@link org.eclipse.steady.backend.model.Library} object.
   * @param bug_id a {@link java.lang.Long} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT"
          + "   DISTINCT t FROM"
          + "   Trace t,"
          + "   Bug b"
          + "   JOIN b.constructChanges cc"
          + "   JOIN cc.constructId c"
          + " WHERE"
          + "   cc.constructId = t.constructId"
          + "   AND t.lib = :lib"
          + "   AND t.app = :app"
          + "   AND b.id = :bug_id"
          + "	  AND NOT (c.type='PACK' OR c.type='CLAS')")
  List<Trace> findVulnerableTracesOfLibraries(
      @Param("app") Application app, @Param("lib") Library lib, @Param("bug_id") Long bug_id);
}

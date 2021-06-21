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
import org.eclipse.steady.backend.model.Dependency;
import org.eclipse.steady.backend.model.DependencyIntersection;
import org.eclipse.steady.backend.model.Space;
import org.eclipse.steady.backend.util.ResultSetFilter;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

// TODO: Make read-only, as construct ids should only be created by other APIs
/**
 * <p>DependencyRepository interface.</p>
 */
@RepositoryRestResource(collectionResourceRel = "dependencies", path = "dependencies")
public interface DependencyRepository
    extends PagingAndSortingRepository<Dependency, Long>, DependencyRepositoryCustom {

  /** Constant <code>FILTER</code> */
  public static final ResultSetFilter<Dependency> FILTER = new ResultSetFilter<Dependency>();

  /**
   * <p>findByDigest.</p>
   *
   * @param lib a {@link java.lang.String} object.
   * @return a {@link java.util.List} object.
   */
  @Query("SELECT dep FROM Dependency dep JOIN FETCH dep.lib l WHERE l.digest = :lib")
  List<Dependency> findByDigest(@Param("lib") String lib);

  /**
   * <p>findByAppAndLib.</p>
   *
   * @param app a {@link org.eclipse.steady.backend.model.Application} object.
   * @param digest a {@link java.lang.String} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT dep FROM Dependency dep JOIN FETCH dep.lib l WHERE dep.app = :app AND dep.lib.digest"
          + " = :digest")
  List<Dependency> findByAppAndLib(@Param("app") Application app, @Param("digest") String digest);

  /**
   * <p>findByAppAndLibAndParentAndRelPath.</p>
   *
   * @param app a {@link org.eclipse.steady.backend.model.Application} object.
   * @param digest a {@link java.lang.String} object.
   * @param dep a {@link org.eclipse.steady.backend.model.Dependency} object.
   * @param relPath a {@link java.lang.String} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT dep FROM Dependency dep JOIN FETCH dep.lib l WHERE (dep.app = :app AND"
          + " dep.lib.digest = :digest AND dep.parent = :parent AND dep.relativePath = :relPath)"
          + " OR (dep.app = :app AND dep.lib.digest = :digest AND dep.parent is null AND"
          + " dep.relativePath = :relPath) OR (dep.app = :app AND dep.lib.digest = :digest AND"
          + " dep.parent is null AND dep.relativePath is null) OR (dep.app = :app AND"
          + " dep.lib.digest = :digest AND dep.parent = :parent AND dep.relativePath is null ) ")
  List<Dependency> findByAppAndLibAndParentAndRelPath(
      @Param("app") Application app,
      @Param("digest") String digest,
      @Param("parent") Dependency dep,
      @Param("relPath") String relPath);

  /**
   * <p>findByApp.</p>
   *
   * @param app a {@link org.eclipse.steady.backend.model.Application} object.
   * @return a {@link java.util.List} object.
   */
  @Query("SELECT dep FROM Dependency dep JOIN FETCH dep.lib l WHERE dep.app = :app")
  List<Dependency> findByApp(@Param("app") Application app);

  /**
   * <p>findByGAV.</p>
   *
   * @param group a {@link java.lang.String} object.
   * @param artifact a {@link java.lang.String} object.
   * @param version a {@link java.lang.String} object.
   * @param space a {@link org.eclipse.steady.backend.model.Space} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "    SELECT dep FROM  Dependency dep  JOIN FETCH dep.lib l LEFT OUTER JOIN FETCH l.libraryId"
          + "  JOIN dep.app WHERE dep.app.mvnGroup = :mvnGroup AND dep.app.artifact = :artifact"
          + " AND dep.app.version = :version AND dep.app.space = :space")
  List<Dependency> findByGAV(
      @Param("mvnGroup") String group,
      @Param("artifact") String artifact,
      @Param("version") String version,
      @Param("space") Space space);

  /**
   * Finds all {@link DependencyIntersection}s for a given {@link Application}.
   *
   * @param group a {@link java.lang.String} object.
   * @param artifact a {@link java.lang.String} object.
   * @param version a {@link java.lang.String} object.
   * @param space a {@link org.eclipse.steady.backend.model.Space} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT   new org.eclipse.steady.backend.model.DependencyIntersection(d1, d2, COUNT(lc1))"
          + " FROM 	    Dependency d1      JOIN      d1.app a1 	    JOIN      d1.lib l1     JOIN  "
          + "    l1.constructs lc1, 	    Dependency d2      JOIN      d2.app a2 	    JOIN     "
          + " d2.lib l2     JOIN      l2.constructs lc2 	  WHERE a1.mvnGroup"
          + " = :mvnGroup " // Given application
          + "     AND a1.artifact = :artifact "
          + "     AND a1.version  = :version "
          + "     AND a1.space  = :space "
          + "     AND a1.mvnGroup = a2.mvnGroup " // Same application
          + "     AND a1.artifact = a2.artifact "
          + "     AND a1.version  = a2.version "
          + "     AND a1.space  = a2.space "
          + "     AND lc1.type = 'CLAS'" // Only classes matter
          + "     AND lc2.type = 'CLAS'"
          + "     AND lc1.qname = lc2.qname " // Same qname
          + "     AND l1.libraryId <> l2.libraryId " // From different libs
  )
  List<DependencyIntersection> findDepIntersections(
      @Param("mvnGroup") String group,
      @Param("artifact") String artifact,
      @Param("version") String version,
      @Param("space") Space space);

  /**
   * <p>findWithBundledByApp.</p>
   *
   * @param app a {@link org.eclipse.steady.backend.model.Application} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT distinct dep FROM Dependency dep JOIN FETCH dep.lib l JOIN FETCH l.bundledLibraryIds"
          + "  lb WHERE dep.app = :app AND lb IS NOT NULL ")
  List<Dependency> findWithBundledByApp(@Param("app") Application app);

  /**
   * <p>findWithDifferentBundledLibByApp.</p>
   *
   * @param app a {@link org.eclipse.steady.backend.model.Application} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT distinct dep FROM Dependency dep   JOIN FETCH dep.lib l   JOIN FETCH"
          + " l.bundledLibraryIds lb   WHERE dep.app = :app AND lb IS NOT NULL AND NOT"
          + " (SIZE(l.bundledLibraryIds)=1 AND l.libraryId MEMBER OF l.bundledLibraryIds) ")
  List<Dependency> findWithDifferentBundledLibByApp(@Param("app") Application app);
}

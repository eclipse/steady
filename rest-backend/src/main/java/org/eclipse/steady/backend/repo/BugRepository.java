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

import org.eclipse.steady.backend.model.Bug;
import org.eclipse.steady.backend.model.Library;
import org.eclipse.steady.backend.model.LibraryId;
import org.eclipse.steady.backend.util.ResultSetFilter;
import org.eclipse.steady.shared.enums.ProgrammingLanguage;
import org.springframework.cache.annotation.Cacheable;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * <p>BugRepository interface.</p>
 */
@Repository
// @RepositoryRestResource(collectionResourceRel = "bugss", path = "bugss")
public interface BugRepository extends CrudRepository<Bug, Long>, BugRepositoryCustom {

  /** Constant <code>FILTER</code> */
  public static final ResultSetFilter<Bug> FILTER = new ResultSetFilter<Bug>();

  /**
   * <p>findByBugId.</p>
   *
   * @param bugid a {@link java.lang.String} object.
   * @return a {@link java.util.List} object.
   */
  @Query("SELECT b FROM Bug b  WHERE b.bugId=:bugId") // adding 'JOIN FETCH b.constructChanges', the
  // junit tests fails: e.g., it tries to insert
  // twice the same bug as if the equal return
  // false?
  List<Bug> findByBugId(@Param("bugId") String bugid);

  /**
   * <p>findCoverageByBugId.</p>
   *
   * @param bugid a {@link java.lang.String} object.
   * @return a {@link java.util.List} object.
   */
  @Query("SELECT b FROM Bug b  WHERE b.bugId=:bugId") // adding 'JOIN FETCH b.constructChanges', the
  // junit tests fails: e.g., it tries to insert
  // twice the same bug as if the equal return
  // false?
  @Cacheable(value = "bug", unless = "#result.isEmpty()")
  List<Bug> findCoverageByBugId(@Param("bugId") String bugid);

  /**
   * <p>findBugByLang.</p>
   *
   * @param lang a {@link org.eclipse.steady.shared.enums.ProgrammingLanguage} object.
   * @return a {@link java.lang.Iterable} object.
   */
  @Query(
      "SELECT distinct b FROM Bug b JOIN b.constructChanges cc JOIN cc.constructId cid WHERE"
          + " cid.lang=:lang")
  Iterable<Bug> findBugByLang(@Param("lang") ProgrammingLanguage lang);

  /**
   * <p>findByLibrary.</p>
   *
   * @param bundledDigest a {@link org.eclipse.steady.backend.model.Library} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT distinct b"
          + "   FROM Library l "
          + "   JOIN "
          + "   l.constructs lc,"
          + "	  Bug b"
          + "   JOIN "
          + "   b.constructChanges cc "
          + "	  WHERE l=:bundledDigest "
          + "   AND lc = cc.constructId"
          + "   AND (NOT lc.type='PACK' " // Java + Python exception
          + "   OR NOT EXISTS (SELECT 1 FROM ConstructChange cc1 JOIN cc1.constructId c1 WHERE"
          + " cc1.bug=cc.bug AND NOT c1.type='PACK' AND NOT c1.qname LIKE '%test%' AND NOT"
          + " c1.qname"
          + " LIKE"
          + " '%Test%'"
          + " and NOT"
          + " cc1.constructChangeType='ADD')"
          + " ) " // select bug if all other cc of the same bug are PACK, ADD or Test changes
          + "   AND NOT (lc.type='MODU' AND (lc.qname='setup' OR lc.qname='tests' OR"
          + " lc.qname='test.__init__'))" // Python-specific exception: setup.py is virtually
  // everywhere, considering it would bring far too many FPs
  )
  List<Bug> findByLibrary(@Param("bundledDigest") Library bundledDigest);

  /**
   * <p>findByLibId.</p>
   *
   * @param bundledLibId a {@link org.eclipse.steady.backend.model.LibraryId} object.
   * @param affected a {@link java.lang.Boolean} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT distinct b FROM "
          + "   LibraryId libid, "
          + "	  Bug b "
          + "   JOIN "
          + "   b.affectedVersions av "
          + "   JOIN "
          + "   av.libraryId av_libid "
          + "   LEFT OUTER JOIN "
          + "   b.constructChanges as cc "
          + "	  WHERE libid = :bundledLibId "
          + "   AND libid = av_libid "
          + "   AND av.affected = :affected"
          + "   AND cc IS NULL")
  List<Bug> findByLibId(
      @Param("bundledLibId") LibraryId bundledLibId, @Param("affected") Boolean affected);
}

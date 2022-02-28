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

import org.eclipse.steady.backend.model.AffectedConstructChange;
import org.eclipse.steady.backend.model.AffectedLibrary;
import org.eclipse.steady.backend.model.Bug;
import org.eclipse.steady.backend.model.Library;
import org.eclipse.steady.backend.model.LibraryId;
import org.eclipse.steady.backend.util.ResultSetFilter;
import org.eclipse.steady.shared.enums.AffectedVersionSource;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * <p>AffectedLibraryRepository interface.</p>
 *
 */
/**
 * <p>AffectedLibraryRepository interface.</p>
 *
 * @author I059406
 */
@Repository
public interface AffectedLibraryRepository
    extends CrudRepository<AffectedLibrary, Long>, AffectedLibraryRepositoryCustom {

  /** Constant <code>FILTER</code> */
  public static final ResultSetFilter<AffectedLibrary> FILTER =
      new ResultSetFilter<AffectedLibrary>();

  /**
   * Find all entries for a given {@link Bug}, {@link LibraryId} and {@link AffectedVersionSource}.
   *
   * @param bug a {@link org.eclipse.steady.backend.model.Bug} object.
   * @param group a {@link java.lang.String} object.
   * @param artifact a {@link java.lang.String} object.
   * @param version a {@link java.lang.String} object.
   * @param source a {@link org.eclipse.steady.shared.enums.AffectedVersionSource} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT afflib FROM AffectedLibrary AS afflib WHERE afflib.bugId = :bug AND"
          + " afflib.libraryId.mvnGroup = :group AND afflib.libraryId.artifact = :artifact  AND"
          + " afflib.libraryId.version = :version AND afflib.source = :source")
  List<AffectedLibrary> findByBugAndLibraryIdAndSource(
      @Param("bug") Bug bug,
      @Param("group") String group,
      @Param("artifact") String artifact,
      @Param("version") String version,
      @Param("source") AffectedVersionSource source);

  /**
   * Find all entries for a given {@link Bug}, Group, Artifact and {@link AffectedVersionSource}.
   *
   * @param bug a {@link org.eclipse.steady.backend.model.Bug} object.
   * @param group a {@link java.lang.String} object.
   * @param artifact a {@link java.lang.String} object.
   * @param source a {@link org.eclipse.steady.shared.enums.AffectedVersionSource} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT afflib FROM AffectedLibrary AS afflib WHERE afflib.bugId = :bug AND"
          + " afflib.libraryId.mvnGroup = :group AND afflib.libraryId.artifact = :artifact AND"
          + " afflib.source = :source")
  List<AffectedLibrary> findByBugAndGAAndSource(
      @Param("bug") Bug bug,
      @Param("group") String group,
      @Param("artifact") String artifact,
      @Param("source") AffectedVersionSource source);

  /**
   * Find all entries for a given {@link Bug} and {@link AffectedVersionSource}.
   *
   * @param bug a {@link org.eclipse.steady.backend.model.Bug} object.
   * @param source a {@link org.eclipse.steady.shared.enums.AffectedVersionSource} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT afflib FROM AffectedLibrary AS afflib WHERE afflib.bugId = :bug AND afflib.source ="
          + " :source")
  List<AffectedLibrary> findByBugAndSource(
      @Param("bug") Bug bug, @Param("source") AffectedVersionSource source);

  /**
   * For a given {@link Bug} and {@link AffectedVersionSource}, find all entries that are linked to a library whose digest is well known.
   *
   * @param bug a {@link org.eclipse.steady.backend.model.Bug} object.
   * @param source a {@link org.eclipse.steady.shared.enums.AffectedVersionSource} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT afflib FROM AffectedLibrary AS afflib JOIN afflib.libraryId lid, Library l "
          + "WHERE l.libraryId=lid AND l.wellknownDigest=true AND"
          + " afflib.bugId = :bug AND afflib.source = :source")
  List<AffectedLibrary> findWellKnownByBugAndSource(
      @Param("bug") Bug bug, @Param("source") AffectedVersionSource source);

  /**
   * <p>findByBugAndLibIdAndSource.</p>
   *
   * @param bug a {@link org.eclipse.steady.backend.model.Bug} object.
   * @param source a {@link org.eclipse.steady.shared.enums.AffectedVersionSource} object.
   * @param group a {@link java.lang.String} object.
   * @param artifact a {@link java.lang.String} object.
   * @param version a {@link java.lang.String} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT afflib FROM AffectedLibrary AS afflib WHERE afflib.bugId = :bug AND afflib.source ="
          + " :source AND afflib.libraryId.mvnGroup = :group AND afflib.libraryId.artifact ="
          + " :artifact AND afflib.libraryId.version = :version")
  List<AffectedLibrary> findByBugAndLibIdAndSource(
      @Param("bug") Bug bug,
      @Param("source") AffectedVersionSource source,
      @Param("group") String group,
      @Param("artifact") String artifact,
      @Param("version") String version);

  /**
   * Find all entries for a given {@link Bug} and {@link AffectedVersionSource}.
   *
   * @param bug a {@link org.eclipse.steady.backend.model.Bug} object.
   * @param source a {@link org.eclipse.steady.shared.enums.AffectedVersionSource} object.
   */
  @Modifying
  @Transactional
  @Query(
      "DELETE FROM AffectedLibrary AS afflib WHERE afflib.bugId = :bug AND afflib.source = :source")
  void deleteByBugAndSource(@Param("bug") Bug bug, @Param("source") AffectedVersionSource source);

  /**
   * Find all entries for a given {@link Bug} and {@link LibraryId}.
   * @param bug
   * @param libraryId
   * @return
   */
  // @Query("SELECT afflib FROM AffectedLibrary AS afflib WHERE afflib.bugId = :bug AND
  // afflib.libraryId.mvnGroup = :group AND afflib.libraryId.artifact = :artifact  AND
  // afflib.libraryId.version = :version")
  // List<AffectedLibrary> findByBugAndLibraryId(@Param("bug") Bug bug, @Param("group") String
  // group, @Param("artifact") String artifact, @Param("version") String version);

  /**
   * Find all entries for a given {@link Bug}.
   *
   * @param bug a {@link org.eclipse.steady.backend.model.Bug} object.
   * @return a {@link java.util.List} object.
   */
  @Query("SELECT afflib FROM AffectedLibrary AS afflib WHERE afflib.bugId = :bug")
  List<AffectedLibrary> findByBug(@Param("bug") Bug bug);

  /**
   * For a given {@link Bug} and {@link AffectedVersionSource}, find all entries that are linked to a library whose digest is well known.
   *
   * @param bug a {@link org.eclipse.steady.backend.model.Bug} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT afflib FROM AffectedLibrary AS afflib JOIN afflib.libraryId lid, Library l "
          + "WHERE l.libraryId=lid AND l.wellknownDigest=true AND"
          + " afflib.bugId = :bug")
  List<AffectedLibrary> findWellKnownByBug(@Param("bug") Bug bug);

  /**
   * <p>findByBugAndLibId.</p>
   *
   * @param bug a {@link org.eclipse.steady.backend.model.Bug} object.
   * @param group a {@link java.lang.String} object.
   * @param artifact a {@link java.lang.String} object.
   * @param version a {@link java.lang.String} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT afflib FROM AffectedLibrary AS afflib WHERE afflib.bugId = :bug AND"
          + " afflib.libraryId.mvnGroup = :group AND afflib.libraryId.artifact = :artifact AND"
          + " afflib.libraryId.version = :version")
  List<AffectedLibrary> findByBugAndLibId(
      @Param("bug") Bug bug,
      @Param("group") String group,
      @Param("artifact") String artifact,
      @Param("version") String version);

  /**
   * <p>findByBugAndGA.</p>
   *
   * @param bug a {@link org.eclipse.steady.backend.model.Bug} object.
   * @param group a {@link java.lang.String} object.
   * @param artifact a {@link java.lang.String} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT afflib FROM AffectedLibrary AS afflib WHERE afflib.bugId = :bug AND"
          + " afflib.libraryId.mvnGroup = :group AND afflib.libraryId.artifact = :artifact")
  List<AffectedLibrary> findByBugAndGA(
      @Param("bug") Bug bug, @Param("group") String group, @Param("artifact") String artifact);

  /**
   * <p>findByBugAndLibAndSource.</p>
   *
   * @param bug a {@link org.eclipse.steady.backend.model.Bug} object.
   * @param digest a {@link org.eclipse.steady.backend.model.Library} object.
   * @param source a {@link org.eclipse.steady.shared.enums.AffectedVersionSource} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT afflib FROM AffectedLibrary AS afflib WHERE afflib.bugId = :bug AND afflib.source ="
          + " :source AND afflib.lib = :digest")
  List<AffectedLibrary> findByBugAndLibAndSource(
      @Param("bug") Bug bug,
      @Param("digest") Library digest,
      @Param("source") AffectedVersionSource source);

  /**
   * <p>deleteCCByAffLib.</p>
   *
   * @param aff_lib a {@link org.eclipse.steady.backend.model.AffectedLibrary} object.
   */
  @Modifying
  @Transactional
  @Query("DELETE FROM AffectedConstructChange as affcc WHERE affcc.affectedLib = :aff_lib")
  void deleteCCByAffLib(@Param("aff_lib") AffectedLibrary aff_lib);

  /**
   * <p>findByBugAndLib.</p>
   *
   * @param bug a {@link org.eclipse.steady.backend.model.Bug} object.
   * @param digest a {@link org.eclipse.steady.backend.model.Library} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT affcc FROM AffectedConstructChange AS affcc JOIN affcc.affectedLib afflib WHERE"
          + " afflib.bugId = :bug AND afflib.lib = :digest")
  List<AffectedConstructChange> findByBugAndLib(
      @Param("bug") Bug bug, @Param("digest") Library digest);

  /**
   * Finds all bugs for a given {@link LibraryId}.
   *
   * @param group a {@link java.lang.String} object.
   * @param artifact a {@link java.lang.String} object.
   * @param version a {@link java.lang.String} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT distinct afflib.bugId FROM AffectedLibrary AS afflib WHERE afflib.libraryId.mvnGroup"
          + " = :group AND afflib.libraryId.artifact = :artifact AND afflib.libraryId.version ="
          + " :version")
  List<Bug> findBugByLibraryId(
      @Param("group") String group,
      @Param("artifact") String artifact,
      @Param("version") String version);
  //	to consider also '?' we can use the following query on our custom view
  //	@Query("SELECT distinct bug FROM v_libraryid_bugs AS afflib WHERE afflib.mvnGroup = :group AND
  // afflib.artifact = :artifact  AND afflib.version = :version AND afflib.affected=:affected",
  // nativeQuery=true)
  //	List<String> findBugByLibraryId(@Param("group") String group, @Param("artifact") String
  // artifact, @Param("version") String version, @Param("affected")Boolean affected);

  /**
   * Same as {@link AffectedLibraryRepository#findBugByLibraryId(String, String, String)}, but offering an additional filter for selected bug ID(s).
   *
   * @param group a {@link java.lang.String} object.
   * @param artifact a {@link java.lang.String} object.
   * @param version a {@link java.lang.String} object.
   * @param bugIds an array of {@link java.lang.String} objects.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT distinct afflib.bugId FROM AffectedLibrary AS afflib WHERE afflib.libraryId.mvnGroup"
          + " = :group AND afflib.libraryId.artifact = :artifact AND afflib.libraryId.version ="
          + " :version AND afflib.bugId.bugId IN :bugIds ")
  List<Bug> findBugByLibraryId(
      @Param("group") String group,
      @Param("artifact") String artifact,
      @Param("version") String version,
      @Param("bugIds") String[] bugIds);

  /**
   * <p>findByLibraryId.</p>
   *
   * @param group a {@link java.lang.String} object.
   * @param artifact a {@link java.lang.String} object.
   * @param version a {@link java.lang.String} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT afflib FROM AffectedLibrary AS afflib WHERE afflib.libraryId.mvnGroup = :group AND"
          + " afflib.libraryId.artifact = :artifact  AND afflib.libraryId.version = :version")
  List<AffectedLibrary> findByLibraryId(
      @Param("group") String group,
      @Param("artifact") String artifact,
      @Param("version") String version);

  /**
   * <p>findByLibraryIdGA.</p>
   *
   * @param group a {@link java.lang.String} object.
   * @param artifact a {@link java.lang.String} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT afflib FROM AffectedLibrary AS afflib WHERE afflib.libraryId.mvnGroup = :group AND"
          + " afflib.libraryId.artifact = :artifact ")
  List<AffectedLibrary> findByLibraryIdGA(
      @Param("group") String group, @Param("artifact") String artifact);

  /**
   * <p>isBugLibAffected.</p>
   * Native query that selects the affected flag from bug_affected_library table.
   * It selects affected libraries defined for libraries (digests) according to
   * the following strategy priority : MANUAL &gt; KAYBEE &gt; CHECK_CODE &gt;
   * AST_EQUALITY|MINOR_EQUALITY|MAJOR_EQUALITY|INTERSECTION|GREATER_RELEASE|PROPAGATE_MANUAL
   * (The latter are treated at the same level of priority as they are all
   * generated by the patch-lib-analyzer ensuring that only one of them exists at
   * every point in time)
   *
   * @param bug_id a {@link java.lang.String} object.
   * @param lib    a {@link java.lang.String} object.
   * @return a {@link java.lang.Boolean} object.
   */
  @Query(
      value =
          " select a.affected from  (select distinct bug_id,lib,affected from bug_affected_library"
              + " where source='MANUAL' and library_id is null  UNION  select distinct"
              + " al1.bug_id,al1.lib,al1.affected from bug_affected_library as al1   where"
              + " al1.library_id is null and (al1.source='KAYBEE')   and not exists (select 1 from"
              + " bug_affected_library as al2 where al2.source='MANUAL' and al1.bug_id=al2.bug_id"
              + " and al1.lib=al2.lib) UNION  select distinct al1.bug_id,al1.lib,al1.affected from"
              + " bug_affected_library as al1   where al1.library_id is null and"
              + " (al1.source='CHECK_CODE')   and not exists (select 1 from bug_affected_library"
              + " as al2 where (al2.source='MANUAL'or al2.source='KAYBEE') and"
              + " al1.bug_id=al2.bug_id and al1.lib=al2.lib) UNION  select distinct"
              + " al1.bug_id,al1.lib,al1.affected from bug_affected_library as al1 where"
              + " al1.library_id is null and (al1.source='AST_EQUALITY' OR"
              + " al1.source='MINOR_EQUALITY'OR al1.source='MAJOR_EQUALITY' OR"
              + " al1.source='GREATER_RELEASE' OR al1.source='INTERSECTION' OR"
              + " al1.source='PROPAGATE_MANUAL')  and not exists (select 1 from"
              + " bug_affected_library as al2 where (al2.source='MANUAL' OR"
              + " al2.source='CHECK_CODE' OR al2.source='KAYBEE') and al1.bug_id=al2.bug_id and"
              + " al1.lib=al2.lib)) as a  where a.bug_id=:bug_id and a.lib=:lib",
      nativeQuery = true)
  Boolean isBugLibAffected(@Param("bug_id") String bug_id, @Param("lib") String lib);

  // @Query(value= " select a.affected from "
  //        + " (select distinct bug_id,lib,library_id,affected from bug_affected_library where
  // source='MANUAL'  "
  //        + " UNION "
  //        + " select distinct bug_id,lib,library_id,affected from bug_affected_library "
  //        + " where (source='AST_EQUALITY' OR source='MINOR_EQUALITY'OR source='MAJOR_EQUALITY' OR
  // source='GREATER_RELEASE' OR source='INTERSECTION') "
  //        + " and not (lib is not null and exists (select 1 from bug_affected_library where
  // source='MANUAL' and bug_id=bug_id and lib=lib)) "
  //        + " and not (library_id is not null and exists (select 1 from bug_affected_library where
  // source='MANUAL' and bug_id=bug_id and library_id=library_id))) as a "
  //        + " where a.bug_id=:bug_id and a.library_id=:library_id",nativeQuery=true)
  /**
   * <p>isBugLibIdAffected.</p>
   * Native query that selects the affected flag from bug_affected_library table.
   * It selects affected libraries defined for libraryIds (gavs) according to
   * the following strategy priority : MANUAL &gt; KAYBEE &gt; CHECK_CODE &gt;
   * AST_EQUALITY|MINOR_EQUALITY|MAJOR_EQUALITY|INTERSECTION|GREATER_RELEASE|PROPAGATE_MANUAL
   * (The latter are treated at the same level of priority as they are all
   * generated by the patch-lib-analyzer ensuring that only one of them exists at
   * every point in time)
   *
   * @param bug_id a {@link java.lang.String} object.
   * @param library_id a {@link org.eclipse.steady.backend.model.LibraryId} object.
   * @return a {@link java.lang.Boolean} object.
   */
  @Query(
      value =
          " select a.affected from  (select distinct bug_id,library_id,affected from"
              + " bug_affected_library where source='MANUAL'  and lib is null  UNION  select"
              + " distinct al1.bug_id,al1.library_id,al1.affected from bug_affected_library as al1"
              + " where al1.lib is null and (al1.source='KAYBEE')   and not exists (select 1 from"
              + " bug_affected_library as al2 where al2.source='MANUAL' and al1.bug_id=al2.bug_id"
              + " and al1.library_id=al2.library_id) UNION  select distinct"
              + " al1.bug_id,al1.library_id,al1.affected from bug_affected_library as al1 where"
              + " al1.lib is null and (al1.source='CHECK_CODE')   and not exists (select 1 from"
              + " bug_affected_library as al2 where (al2.source='MANUAL' or al2.source='KAYBEE')"
              + " and al1.bug_id=al2.bug_id and al1.library_id=al2.library_id) UNION  select"
              + " distinct al1.bug_id,al1.library_id,al1.affected from bug_affected_library as al1"
              + " where al1.lib is null and (al1.source='AST_EQUALITY' OR"
              + " al1.source='MINOR_EQUALITY'OR al1.source='MAJOR_EQUALITY' OR"
              + " al1.source='GREATER_RELEASE' OR al1.source='INTERSECTION' OR"
              + " al1.source='PROPAGATE_MANUAL')  and not exists (select 1 from"
              + " bug_affected_library as al2 where (al2.source='MANUAL' OR"
              + " al2.source='CHECK_CODE' OR al2.source='KAYBEE') and al1.bug_id=al2.bug_id and"
              + " al1.library_id=al2.library_id)) as a  where a.bug_id=:bug_id and"
              + " a.library_id=:library_id",
      nativeQuery = true)
  Boolean isBugLibIdAffected(
      @Param("bug_id") String bug_id, @Param("library_id") LibraryId library_id);

  /**
   * <p>findResolvedAffectedLibrary.</p>
   * Native query that selects the affected library applicable for a given libraryId given
   * the following strategy priority : MANUAL &gt; KAYBEE &gt; CHECK_CODE &gt;
   * AST_EQUALITY|MINOR_EQUALITY|MAJOR_EQUALITY|INTERSECTION|GREATER_RELEASE|PROPAGATE_MANUAL
   * (The latter are treated at the same level of priority as they are all
   * generated by the patch-lib-analyzer ensuring that only one of them exists at
   * every point in time)
   *
   * @param bug_id a {@link java.lang.String} object.
   * @param library_id a {@link org.eclipse.steady.backend.model.LibraryId} object.
   * @return a {@link java.lang.Boolean} object.
   */
  @Query(
      value =
          " select * from  (select distinct * from bug_affected_library where source='MANUAL'  and"
              + " lib is null  UNION  select distinct * from bug_affected_library as al1 where"
              + " al1.lib is null and (al1.source='KAYBEE')   and not exists (select 1 from"
              + " bug_affected_library as al2 where al2.source='MANUAL' and al1.bug_id=al2.bug_id"
              + " and al1.library_id=al2.library_id) UNION  select distinct * from"
              + " bug_affected_library as al1 where al1.lib is null and (al1.source='CHECK_CODE') "
              + "  and not exists (select 1 from bug_affected_library as al2 where"
              + " (al2.source='MANUAL' or al2.source='KAYBEE') and al1.bug_id=al2.bug_id and"
              + " al1.library_id=al2.library_id) UNION  select distinct * from"
              + " bug_affected_library as al1 where al1.lib is null and (al1.source='AST_EQUALITY'"
              + " OR al1.source='MINOR_EQUALITY'OR al1.source='MAJOR_EQUALITY' OR"
              + " al1.source='GREATER_RELEASE' OR al1.source='INTERSECTION' OR"
              + " al1.source='PROPAGATE_MANUAL')  and al1.created_at=(select max(created_at) from"
              + " bug_affected_library where bug_id=:bug_id and library_id=:library_id and not"
              + " source='TO_REVIEW') and not exists (select 1 from bug_affected_library as al2"
              + " where (al2.source='MANUAL' OR al2.source='CHECK_CODE' OR al2.source='KAYBEE')"
              + " and al1.bug_id=al2.bug_id and al1.library_id=al2.library_id)) as a  where"
              + " a.bug_id=:bug_id and a.library_id=:library_id",
      nativeQuery = true)
  AffectedLibrary findResolvedAffectedLibrary(
      @Param("bug_id") String bug_id, @Param("library_id") LibraryId library_id);
}

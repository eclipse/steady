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
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sap.psr.vulas.backend.repo;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sap.psr.vulas.backend.model.AffectedConstructChange;
import com.sap.psr.vulas.backend.model.Application;
import com.sap.psr.vulas.backend.model.Bug;
import com.sap.psr.vulas.backend.model.ConstructId;
import com.sap.psr.vulas.backend.model.Library;
import com.sap.psr.vulas.backend.model.LibraryId;
import com.sap.psr.vulas.backend.util.ResultSetFilter;
import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.shared.enums.Scope;

/**
 * See here for JPQL: http://docs.oracle.com/javaee/6/tutorial/doc/bnbtg.html
 */
@Repository
// @RepositoryRestResource(collectionResourceRel = "bugss", path = "bugss")
public interface LibraryRepository extends CrudRepository<Library, Long>, LibraryRepositoryCustom {

    /** Constant <code>FILTER</code> */
    public static final ResultSetFilter<Library> FILTER = new ResultSetFilter<Library>();

    /**
     * <p>findById.</p>
     *
     * @param id a {@link java.lang.Long} object.
     * @return a {@link java.util.List} object.
     */
    @Query("SELECT l FROM Library l LEFT OUTER JOIN FETCH l.libraryId WHERE l.id=:id")
    List<Library> findById(@Param("id") Long id);

    /**
     * <p>findByDigest.</p>
     *
     * @param digest a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    @Query("SELECT l FROM Library l LEFT OUTER JOIN FETCH l.libraryId WHERE l.digest=:digest")
    List<Library> findByDigest(@Param("digest") String digest);

    /**
     * <p>countConstructOfLibrary.</p>
     *
     * @param id a {@link java.lang.Long} object.
     * @return a {@link java.lang.Integer} object.
     */
    @Query(
            value = "SELECT COUNT(*) FROM " + " lib_constructs  " + "  WHERE library_id =:id ",
            nativeQuery = true)
    Integer countConstructOfLibrary(@Param("id") Long id);

    /**
     * <p>countUsages.</p>
     *
     * @param digest a {@link java.lang.String} object.
     * @return a {@link java.lang.Integer} object.
     */
    @Query(
            value =
                    "SELECT COUNT(distinct app) FROM "
                            + " app_dependency  "
                            + "  WHERE lib =:digest and transitive=false ",
            nativeQuery = true)
    Integer countUsages(@Param("digest") String digest);

    //	@Query(value="SELECT COUNT(distinct d.app) FROM "
    //			+ " Dependency d  "
    //			+ " WHERE lib =:digest and d.transitive=false AND d.scope NOT IN :exclScopes")
    //	Integer countUsages(@Param("digest") String digest,@Param("exclScopes") Dependency.Scope[]
    // exclScopes);

    /**
     * <p>findMostUsed.</p>
     *
     * @param num a {@link java.lang.Integer} object.
     * @return a {@link java.util.List} object.
     */
    @Query(
            value =
                    "select t.lib from (SELECT COUNT(distinct app) as c,lib  FROM    "
                            + " app_dependency      WHERE  transitive=false group by lib order by c"
                            + " desc) as t limit :num",
            nativeQuery = true)
    List<String> findMostUsed(@Param("num") Integer num);

    /**
     * <p>findMostUsed.</p>
     *
     * @param exclScopes an array of {@link com.sap.psr.vulas.shared.enums.Scope} objects.
     * @return a {@link java.util.List} object.
     */
    @Query(
            value =
                    " SELECT d.lib.digest,COUNT(distinct d.app)  FROM    Dependency d     WHERE "
                            + " transitive=false    AND d.scope NOT IN :exclScopes group by"
                            + " d.lib.digest order by COUNT(distinct d.app) desc")
    List<Object[]> findMostUsed(@Param("exclScopes") Scope[] exclScopes);

    /**
     * Returns all {@link Bug}s relevant for the {@link Library} with the given digest.
     * A {@link Bug} is considered relevant if one or more of the {@link ConstructId}s
     * changed as part of the bug fix is contained in the {@link Library}.
     *
     * @param digest a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    @Query(
            "SELECT"
                    + " DISTINCT b FROM"
                    + "   Bug b"
                    + "   JOIN b.constructChanges cc,"
                    + "   Library l"
                    + "   JOIN l.constructs lc"
                    + " WHERE"
                    + "       cc.constructId = lc"
                    + "   AND l.digest = :digest")
    List<Bug> findBugs(@Param("digest") String digest);

    /**
     * Same as {@link LibraryRepository#findBugs(String)}, but offering an additional filter for selected bug ID(s).
     *
     * @param digest a {@link java.lang.String} object.
     * @param bugIds an array of {@link java.lang.String} objects.
     * @return a {@link java.util.List} object.
     */
    @Query(
            "SELECT"
                    + " DISTINCT b FROM"
                    + "   Bug b"
                    + "   JOIN b.constructChanges cc,"
                    + "   Library l"
                    + "   JOIN l.constructs lc"
                    + " WHERE"
                    + "       cc.constructId = lc"
                    + "   AND l.digest = :digest"
                    + "   AND b.bugId IN :bugIds") // filter clause on bugId
    List<Bug> findBugs(@Param("digest") String digest, @Param("bugIds") String[] bugIds);

    /**
     * Returns all {@link ConstructId}s for the {@link Library} with the given digest and the bug with
     * the given ID that exist both in the library and the bug's change list.
     *
     * @param digest a {@link java.lang.String} object.
     * @param bugId a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    @Query(
            "SELECT"
                    + " DISTINCT lc FROM"
                    + "   Bug b"
                    + "   JOIN b.constructChanges cc,"
                    + "   Library l"
                    + "   JOIN l.constructs lc"
                    + " WHERE"
                    + "       cc.constructId = lc"
                    + "   AND l.digest  = :digest"
                    + "   AND b.bugId = :bugId")
    List<ConstructId> findBuggyConstructIds(
            @Param("digest") String digest, @Param("bugId") String bugId);

    /**
     * Returns all {@link ConstructId}s contained in the {@link Library} with the given digest.
     *
     * @param digest a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    @Query(
            "SELECT"
                    + " DISTINCT lc FROM"
                    + "   Library l"
                    + "   JOIN l.constructs lc"
                    + " WHERE"
                    + "   l.digest = :digest")
    List<ConstructId> findConstructIds(@Param("digest") String digest);

    /**
     * Returns all {@link ConstructId}s contained in the {@link Library} with the given digest and of the given type.
     *
     * @param digest a {@link java.lang.String} object.
     * @param type a {@link com.sap.psr.vulas.shared.enums.ConstructType} object.
     * @return a {@link java.util.List} object.
     */
    @Query(
            "SELECT"
                    + " DISTINCT lc FROM"
                    + "   Library l"
                    + "   JOIN l.constructs lc"
                    + " WHERE"
                    + "   l.digest = :digest AND lc.type=:type")
    List<ConstructId> findConstructIdsOfType(
            @Param("digest") String digest, @Param("type") ConstructType type);

    /**
     * <p>findAffCCs.</p>
     *
     * @param digest a {@link java.lang.String} object.
     * @param bugId a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    @Query(
            "SELECT"
                    + " DISTINCT affcc FROM"
                    + "   AffectedConstructChange affcc"
                    + "   JOIN affcc.affectedLib afflib,"
                    + "	  Bug b"
                    + "   JOIN b.constructChanges cc"
                    + " WHERE"
                    + "       affcc.cc = cc"
                    + "   AND afflib.lib.digest = :digest"
                    + "   AND b.bugId = :bugId")
    List<AffectedConstructChange> findAffCCs(
            @Param("digest") String digest, @Param("bugId") String bugId);

    /**
     * <p>findJPQLVulnerableLibrariesByBug.</p>
     *
     * @param bugId a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    @Query(
            "SELECT"
                    + "   DISTINCT l FROM"
                    + "	  Library l "
                    + "   LEFT OUTER JOIN FETCH"
                    + "   l.libraryId lid "
                    + "   JOIN "
                    + "   l.constructs lc,"
                    + "	  Bug b"
                    + "   JOIN "
                    + "   b.constructChanges cc "
                    + "	  WHERE b.bugId = :bugId"
                    + "   AND lc = cc.constructId"
                    + "   AND (NOT lc.type='PACK' " // Java + Python exception
                    + "   OR NOT EXISTS (SELECT 1 FROM ConstructChange cc1 JOIN cc1.constructId c1"
                    + " WHERE cc1.bug=cc.bug AND NOT c1.type='PACK' AND NOT c1.qname LIKE '%test%'"
                    + " AND NOT c1.qname LIKE '%Test%' and NOT cc1.constructChangeType='ADD') )   "
                    + " AND NOT (lc.type='MODU' AND (lc.qname='setup' OR lc.qname='tests' OR"
                    + " lc.qname='test.__init__'))" // Python-specific exception: setup.py is
    // virtually everywhere, considering it would
    // bring far too many FPs. Similarly tests.py
    // originates such a generic module that would
    // bring up too many FPs
    )
    List<Library> findJPQLVulnerableLibrariesByBug(@Param("bugId") String bugId);

    //	@Query(value= "select distinct s.digest from " +
    //			"(select y.digest,y.library_id_id,y.m,i.n from " +
    // "(select v.digest as digest,v.library_id_id,count(u.id) as m from "+
    // "(select distinct a.digest,a.library_id_id,d.id  from lib as a join lib_constructs as b on
    // a.id=b.lib_id join construct_id as d on b.constructs_id=d.id where d.type='PACK' ) as v "+
    // "join "+
    // "(select c.id from lib as l join lib_constructs as lc on l.id=lc.lib_id join construct_id as
    // c on lc.constructs_id=c.id where l.digest=:digest and c.type='PACK') as u on v.id=u.id group
    // by v.digest,v.library_id_id) as y , "+
    // " (select count(*) as n from lib as l join lib_constructs as lc on l.id=lc.lib_id join
    // construct_id as c on lc.constructs_id=c.id "+
    // "where l.digest=:digest and c.type='PACK' ) as i "+
    // "where (y.m > (i.n *80/100) and  y.m < (i.n *120/100))) as s join library_id as lid on
    // s.library_id_id=lid.id ",nativeQuery=true)
    /**
     * <p>findDigestSamePack.</p>
     *
     * @param digest a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    @Query(
            value =
                    "select distinct s.digest  from  (select candidate.digest, candidate.id,"
                        + " candidate.mvn_group,candidate.artifact,candidate.version,count(*) as"
                        + " candidate_pack_count from  (select distinct"
                        + " a.digest,a.id,a.mvn_group,a.artifact,a.version   from  (select"
                        + " distinct"
                        + " l1.digest,l1.id,lid1.mvn_group,lid1.artifact,lid1.version,c1.id  as"
                        + " cid  from lib as l1   join library_id lid1 on l1.library_id_id=lid1.id"
                        + "  join lib_constructs as lc1 on l1.id=lc1.library_id   join"
                        + " construct_id as c1 on lc1.constructs_id=c1.id where c1.type='PACK' )"
                        + " as a  join  (select c.id   from lib as l  join lib_constructs as lc on"
                        + " l.id=lc.library_id  join construct_id as c on lc.constructs_id=c.id  "
                        + " where l.digest=:digest and c.type='PACK') as unknown  on"
                        + " a.cid=unknown.id) as candidate  join lib_constructs as lc3 on"
                        + " candidate.id=lc3.library_id  join construct_id as c3 on"
                        + " lc3.constructs_id=c3.id   where c3.type='PACK' group by"
                        + " candidate.digest,candidate.id,candidate.mvn_group,candidate.artifact,candidate.version)"
                        + " as s,  (select count(*) as unknown_pack_count  from lib as l2   join"
                        + " lib_constructs as lc2 on l2.id=lc2.library_id  join construct_id as c2"
                        + " on lc2.constructs_id=c2.id   where l2.digest=:digest and"
                        + " c2.type='PACK') as unknown_count  where (s.candidate_pack_count >="
                        + " unknown_count.unknown_pack_count*90/100) AND (s.candidate_pack_count"
                        + " <= unknown_count.unknown_pack_count*100/100) ",
            nativeQuery = true)
    List<String> findDigestSamePack(@Param("digest") String digest);

    /**
     * <p>findByLibraryId.</p>
     *
     * @param bundledLibId a {@link com.sap.psr.vulas.backend.model.LibraryId} object.
     * @return a {@link java.util.List} object.
     */
    @Query("SELECT l FROM Library l WHERE l.libraryId =:bundledLibId")
    List<Library> findByLibraryId(@Param("bundledLibId") LibraryId bundledLibId);

    /**
     * <p>Finds libraries that are rebundled in an application dependency. It returns a list of pairs dependency, bundled library.
     *  Note that bundled libraries must have a digest and a GAV different from the one of the dependency rebundling it.</p>
     *
     * @param app a {@link com.sap.psr.vulas.backend.model.Application} object.
     * @return a {@link java.util.List} object.
     */
    @Query(
            value =
                    "select distinct d.id as dep_id, l2.id as bundled_lib_id   from app_dependency"
                        + " d    inner join lib l1 on d.lib=l1.digest inner join library_id lid1"
                        + " on l1.library_id_id =lid1.id    inner join lib_bundled_library_ids bl"
                        + " on l1.id=bl.library_id inner join library_id lid2 on"
                        + " bl.bundled_library_ids_id=lid2.id    inner join lib l2 on"
                        + " bl.bundled_library_ids_id=l2.library_id_id    where d.app=:app and not"
                        + " l1.id = l2.id and l2.wellknown_digest='true' and not lid1.id=lid2.id",
            nativeQuery = true)
    List<Object[]> findBundledLibByApp(@Param("app") Application app);
}

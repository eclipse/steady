package com.sap.psr.vulas.backend.repo;


import java.util.Collection;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sap.psr.vulas.backend.model.Bug;
import com.sap.psr.vulas.backend.model.Library;
import com.sap.psr.vulas.backend.model.LibraryId;
import com.sap.psr.vulas.backend.util.ResultSetFilter;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;

@Repository
//@RepositoryRestResource(collectionResourceRel = "bugss", path = "bugss")
public interface BugRepository extends CrudRepository<Bug, Long>, BugRepositoryCustom {

	public static final ResultSetFilter<Bug> FILTER = new ResultSetFilter<Bug>();
	
	@Query("SELECT b FROM Bug b JOIN FETCH b.constructChanges WHERE b.id=:id")
	List<Bug> findById(@Param("id") Long id);
	
	@Query("SELECT b FROM Bug b  WHERE b.bugId=:bugId") //adding 'JOIN FETCH b.constructChanges', the junit tests fails: e.g., it tries to insert twice the same bug as if the equal return false?
	List<Bug> findByBugId(@Param("bugId") String bugid);
	
	@Query("SELECT b FROM Bug b  WHERE b.bugId=:bugId") //adding 'JOIN FETCH b.constructChanges', the junit tests fails: e.g., it tries to insert twice the same bug as if the equal return false?
	@Cacheable(value="bug", unless="#result.isEmpty()")
	List<Bug> findCoverageByBugId(@Param("bugId") String bugid);

	@Query("SELECT distinct b FROM Bug b JOIN b.constructChanges cc JOIN cc.constructId cid WHERE cid.lang=:lang")
	Iterable<Bug> findBugByLang(@Param("lang") ProgrammingLanguage lang);

	
	@Query("SELECT distinct b"
			+ "   FROM Library l "
			+ "   JOIN "
			+ "   l.constructs lc,"
			+ "	  Bug b"
			+ "   JOIN "
			+ "   b.constructChanges cc "
			+ "	  WHERE l=:bundledDigest "
			+ "   AND lc = cc.constructId"		
			+ "   AND (NOT lc.type='PACK' "                        // Java + Python exception
			+ "   OR NOT EXISTS (SELECT 1 FROM ConstructChange cc1 JOIN cc1.constructId c1 WHERE cc1.bug=cc.bug AND NOT c1.type='PACK' AND NOT c1.qname LIKE '%test%' AND NOT c1.qname LIKE '%Test%' and NOT cc1.constructChangeType='ADD') ) "      //select bug if all other cc of the same bug are PACK, ADD or Test changes
			+ "   AND NOT (lc.type='MODU' AND lc.qname='setup')" // Python-specific exception: setup.py is virtually everywhere, considering it would bring far too many FPs
			)
	List<Bug> findByLibrary(@Param("bundledDigest") Library bundledDigest);
	
	
	@Query("SELECT distinct b FROM "
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
			+ "   AND cc IS NULL"
			)
	List<Bug> findByLibId(@Param("bundledLibId") LibraryId bundledLibId,@Param("affected") Boolean affected);
}
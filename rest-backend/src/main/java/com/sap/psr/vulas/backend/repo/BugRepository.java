package com.sap.psr.vulas.backend.repo;


import java.util.List;

import org.springframework.cache.annotation.Cacheable;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sap.psr.vulas.backend.model.Bug;
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
}
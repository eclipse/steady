package com.sap.psr.vulas.backend.repo;


import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.sap.psr.vulas.backend.model.Bug;
import com.sap.psr.vulas.backend.model.ConstructChange;
import com.sap.psr.vulas.backend.model.ConstructId;
import com.sap.psr.vulas.backend.util.ResultSetFilter;

//TODO: Make read-only, as construct ids should only be created by other APIs
@RepositoryRestResource(collectionResourceRel = "constructChanges", path = "constructChanges")
public interface ConstructChangeRepository extends PagingAndSortingRepository<ConstructChange, Long> {
	
	public static final ResultSetFilter<ConstructChange> FILTER = new ResultSetFilter<ConstructChange>();
	
	@Query("SELECT c FROM ConstructChange AS c WHERE c.repo=:repo AND "
			+ "c.repoPath =:path AND "
			+ "c.commit =:commit AND "
			+ "c.constructId=:cid AND "
			+ "c.bug =:bug")
	List<ConstructChange> findByRepoPathCommitCidBug(@Param("repo") String repo, 
			@Param("path") String path, @Param("commit") String commit, 
			@Param("cid") ConstructId cid, @Param("bug") Bug bug);
	
}
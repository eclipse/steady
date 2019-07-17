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
/**
 * <p>ConstructChangeRepository interface.</p>
 *
 */
@RepositoryRestResource(collectionResourceRel = "constructChanges", path = "constructChanges")
public interface ConstructChangeRepository extends PagingAndSortingRepository<ConstructChange, Long> {
	
	/** Constant <code>FILTER</code> */
	public static final ResultSetFilter<ConstructChange> FILTER = new ResultSetFilter<ConstructChange>();
	
	/**
	 * <p>findByRepoPathCommitCidBug.</p>
	 *
	 * @param repo a {@link java.lang.String} object.
	 * @param path a {@link java.lang.String} object.
	 * @param commit a {@link java.lang.String} object.
	 * @param cid a {@link com.sap.psr.vulas.backend.model.ConstructId} object.
	 * @param bug a {@link com.sap.psr.vulas.backend.model.Bug} object.
	 * @return a {@link java.util.List} object.
	 */
	@Query("SELECT c FROM ConstructChange AS c WHERE c.repo=:repo AND "
			+ "c.repoPath =:path AND "
			+ "c.commit =:commit AND "
			+ "c.constructId=:cid AND "
			+ "c.bug =:bug")
	List<ConstructChange> findByRepoPathCommitCidBug(@Param("repo") String repo, 
			@Param("path") String path, @Param("commit") String commit, 
			@Param("cid") ConstructId cid, @Param("bug") Bug bug);
	
}

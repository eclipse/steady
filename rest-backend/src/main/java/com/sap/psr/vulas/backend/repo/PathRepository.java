package com.sap.psr.vulas.backend.repo;


import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sap.psr.vulas.backend.model.Application;
import com.sap.psr.vulas.backend.model.Bug;
import com.sap.psr.vulas.backend.model.ConstructId;
import com.sap.psr.vulas.backend.model.Library;
import com.sap.psr.vulas.backend.model.Path;
import com.sap.psr.vulas.backend.util.ResultSetFilter;
import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.enums.PathSource;

@Repository
public interface PathRepository extends CrudRepository<Path, Long>, PathRepositoryCustom {
	
	public static final ResultSetFilter<Path> FILTER = new ResultSetFilter<Path>();
	
	List<Path> findByApp(@Param("app") Application app);
	
	/**
	 * Deletes all paths collected in the context of the given {@link Application}.
	 * Called by goal {@link GoalType#CLEAN}.
	 * @param app
	 */
	@Modifying
	@Transactional
	@Query("DELETE FROM Path AS p WHERE p.app = :app")
	void deleteAllPathsForApp(@Param("app") Application app);
	
	@Query("SELECT p FROM Path AS p WHERE p.app = :app")
	List<Path> findPathsForApp(@Param("app") Application app);
	
	//@Query("SELECT p FROM Path AS p WHERE p.app = :app AND p.lib = :lib AND p.bug = :bug")
	//List<Path> findPathsForLibraryBug(@Param("app") Application app, @Param("lib") Library lib, @Param("bug") Bug bug);
	
	@Query("SELECT p FROM Path AS p WHERE p.app = :app AND p.lib = :lib AND p.bug.id = :bug_id")
	List<Path> findPathsForLibraryBug(@Param("app") Application app, @Param("lib") Library lib, @Param("bug_id") Long bug_id);
	
	@Query("SELECT p FROM Path p WHERE (p.endConstructId.qname = :qname OR p.startConstructId.qname = :qname) AND p.app = :app AND p.lib = :lib AND p.bug = :bug")
	List<Path> findPathsForLibraryBugConstructName(@Param("app") Application app, @Param("lib") Library lib, @Param("bug") Bug bug, @Param("qname") String qname);
	
	@Query("SELECT p FROM Path AS p WHERE p.app = :app AND p.bug = :bug AND p.source = :source AND p.startConstructId = :startConstructId AND p.endConstructId = :endConstructId")
	List<Path> findPath(@Param("app") Application app, @Param("bug") Bug bug, @Param("source") PathSource source, @Param("startConstructId") ConstructId startConstructId, @Param("endConstructId") ConstructId endConstructId);
}
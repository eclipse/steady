package com.sap.psr.vulas.backend.repo;


import java.util.List;
import java.util.Optional;

import javax.validation.constraints.Null;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.sap.psr.vulas.backend.model.Application;
import com.sap.psr.vulas.backend.model.Dependency;
import com.sap.psr.vulas.backend.model.DependencyIntersection;
import com.sap.psr.vulas.backend.model.Space;
import com.sap.psr.vulas.backend.util.ResultSetFilter;

//TODO: Make read-only, as construct ids should only be created by other APIs
@RepositoryRestResource(collectionResourceRel = "dependencies", path = "dependencies")
public interface DependencyRepository extends PagingAndSortingRepository<Dependency, Long>, DependencyRepositoryCustom {
	
	public static final ResultSetFilter<Dependency> FILTER = new ResultSetFilter<Dependency>();

//	@Query("SELECT dep FROM Dependency dep JOIN FETCH dep.lib l WHERE dep.id = :id")
//	Optional<List<Dependency>> findById(@Param("id") ID id);
	
	@Query("SELECT dep FROM Dependency dep JOIN FETCH dep.lib l WHERE l.digest = :lib")
	List<Dependency> findByDigest(@Param("lib") String lib);
	
	@Query("SELECT dep FROM Dependency dep JOIN FETCH dep.lib l WHERE dep.app = :app AND dep.lib.digest = :digest")
	List<Dependency> findByAppAndLib(@Param("app") Application app, @Param("digest") String digest);
	
	@Query("SELECT dep FROM Dependency dep JOIN FETCH dep.lib l WHERE "
			+ "(dep.app = :app AND dep.lib.digest = :digest AND dep.parent = :parent AND dep.relativePath = :relPath) OR "
			+ "(dep.app = :app AND dep.lib.digest = :digest AND dep.parent is null AND dep.relativePath = :relPath) OR "
			+ "(dep.app = :app AND dep.lib.digest = :digest AND dep.parent is null AND dep.relativePath is null) OR "
			+ "(dep.app = :app AND dep.lib.digest = :digest AND dep.parent = :parent AND dep.relativePath is null ) ")
	List<Dependency> findByAppAndLibAndParentAndRelPath(@Param("app") Application app, @Param("digest") String digest, @Param("parent") Dependency dep, @Param("relPath") String relPath);
	
	@Query("SELECT dep FROM Dependency dep JOIN FETCH dep.lib l WHERE dep.app = :app")
	List<Dependency> findByApp(@Param("app") Application app);
	
	@Query("    SELECT dep FROM "
			+ " Dependency dep "
			+ " JOIN FETCH dep.lib l"
			+ " LEFT OUTER JOIN FETCH l.libraryId "
			+ " JOIN dep.app"
			+ " WHERE dep.app.mvnGroup = :mvnGroup AND dep.app.artifact = :artifact AND dep.app.version = :version AND dep.app.space = :space")
	List<Dependency> findByGAV(@Param("mvnGroup") String group, @Param("artifact") String artifact,@Param("version") String version, @Param("space") Space space);

	/**
	 * Finds all {@link DependencyIntersection}s for a given {@link Application}.
	 * @param group
	 * @param artifact
	 * @param version
	 * @return
	 */
	@Query("SELECT"
			+ "   new com.sap.psr.vulas.backend.model.DependencyIntersection(d1, d2, COUNT(lc1)) FROM "
			+ "	    Dependency d1 "
			+ "     JOIN "
			+ "     d1.app a1 "
			+ "	    JOIN "
			+ "     d1.lib l1"
			+ "     JOIN "
			+ "     l1.constructs lc1, "
			+ "	    Dependency d2 "
			+ "     JOIN "
			+ "     d2.app a2 "
			+ "	    JOIN "
			+ "     d2.lib l2"
			+ "     JOIN "
			+ "     l2.constructs lc2 "
			+ "	  WHERE a1.mvnGroup = :mvnGroup "      // Given application
			+ "     AND a1.artifact = :artifact "
			+ "     AND a1.version  = :version "
			+ "     AND a1.space  = :space "
			+ "     AND a1.mvnGroup = a2.mvnGroup "    // Same application
			+ "     AND a1.artifact = a2.artifact "
			+ "     AND a1.version  = a2.version "
			+ "     AND a1.space  = a2.space "
			+ "     AND lc1.type = 'CLAS'"             // Only classes matter
			+ "     AND lc2.type = 'CLAS'"
			+ "     AND lc1.qname = lc2.qname "		   // Same qname
			+ "     AND l1.libraryId <> l2.libraryId " // From different libs
			)
	List<DependencyIntersection> findDepIntersections(@Param("mvnGroup") String group, @Param("artifact") String artifact,@Param("version") String version,@Param("space") Space space);
	
}
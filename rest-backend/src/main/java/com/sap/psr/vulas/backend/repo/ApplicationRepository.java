package com.sap.psr.vulas.backend.repo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sap.psr.vulas.backend.model.Application;
import com.sap.psr.vulas.backend.model.Bug;
import com.sap.psr.vulas.backend.model.ConstructId;
import com.sap.psr.vulas.backend.model.ConstructSearchResult;
import com.sap.psr.vulas.backend.model.Dependency;
import com.sap.psr.vulas.backend.model.LibraryId;
import com.sap.psr.vulas.backend.model.Space;
import com.sap.psr.vulas.backend.model.Tenant;
import com.sap.psr.vulas.backend.model.VulnerableDependency;
import com.sap.psr.vulas.backend.util.ResultSetFilter;
import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.shared.enums.Scope;


/**
 * 
 * TODO: Read with token by adding a where-clause AND (a.token = :token OR true = :ignoreToken), whereby ignoreToken is a boolean argument set according to TokenUtil.isIgnoreToken
 */
@Repository
public interface ApplicationRepository extends CrudRepository<Application, Long>, ApplicationRepositoryCustom {
	
	public static final ResultSetFilter<Application> FILTER = new ResultSetFilter<Application>();
	
//	List<Application> findById(@Param("id") Long id);
		
	@Query("SELECT app FROM Application AS app JOIN FETCH app.space s WHERE app.mvnGroup = :mvnGroup AND app.artifact = :artifact AND app.space = :space")
	List<Application> findByGA(@Param("mvnGroup") String group, @Param("artifact") String artifact, @Param("space") Space space);
	
	@Query("SELECT app FROM Application AS app JOIN FETCH app.space s WHERE app.mvnGroup = :mvnGroup AND app.artifact = :artifact AND app.version = :version AND app.space = :space")
	List<Application> findByGAV(@Param("mvnGroup") String group, @Param("artifact") String artifact,@Param("version") String version, @Param("space") Space space);
	

	//@Query("SELECT DISTINCT app FROM Application AS app JOIN FETCH app.space s WHERE s.spaceToken=:spaceToken AND app.lastVulnChange >  :timestamp ORDER BY app.mvnGroup, app.artifact, app.version")
	@Query(value="select distinct a.id, a.artifact, a.created_at, a.modified_at, a.last_vuln_change, a.last_scan, "
			+ " a.mvn_group, a.space, a.version "
			+ " from app a  inner join space s on a.space=s.id "
			+ " where s.space_token= :spaceToken and (extract(epoch from last_vuln_change) > :timestamp OR extract(epoch from last_scan) > :timestamp)", nativeQuery=true)
	List<Application> findAllApps(@Param("spaceToken") String spaceToken, @Param("timestamp") long timestamp);

	/**
	 * Returns all {@link Application}s of the given {@link Tenant}, no matter the {@link Space}. Note that the JPQL query cannot use distinct, as the applications
	 * of different spaces can have the same group, artifact and version identifier.
	 * @param tenant
	 * @return
	 */
	//@Query("SELECT DISTINCT app FROM Application AS app JOIN FETCH app.space s WHERE s.tenant = :tenant ORDER BY app.mvnGroup, app.artifact, app.version")
	@Query("SELECT app FROM Application AS app JOIN FETCH app.space s WHERE s.tenant = :tenant ORDER BY app.mvnGroup, app.artifact, app.version")
	ArrayList<Application> findAllApps(@Param("tenant") Tenant tenant);
	
	/**
	 * Returns all {@link Application}s of the given {@link Tenant} and {@link Space}. Note that the JPQL query cannot use distinct, as the applications
	 * of different spaces can have the same group, artifact and version identifier.
	 * @param tenant
	 * @return
	 */
	@Query("SELECT app FROM Application AS app JOIN FETCH app.space s WHERE s.tenant = :tenant AND s.spaceToken = :spaceToken ORDER BY app.mvnGroup, app.artifact, app.version")
	ArrayList<Application> findAllApps(@Param("tenant") Tenant tenant, @Param("spaceToken") String spaceToken);
	
	@Query("SELECT app FROM Application AS app JOIN FETCH app.space s WHERE app.mvnGroup LIKE :mvnGroup AND app.artifact LIKE :artifact AND app.version LIKE :version")
	Collection<Application> searchByGAV(@Param("mvnGroup") String group, @Param("artifact") String artifact,@Param("version") String version);
	
//	@Query("SELECT DISTINCT app FROM Application AS app JOIN app.dependencies order by app.mvnGroup,app.artifact,app.version")
//	Iterable<Application> findAppsWithDep();

	@Query(value="(select distinct applicatio0_.id, applicatio0_.artifact, applicatio0_.created_at,  applicatio0_.modified_at, applicatio0_.last_vuln_change, applicatio0_.last_scan, "
			+ " applicatio0_.mvn_group, applicatio0_.space, applicatio0_.version "
			+ " from app applicatio0_  inner join space space3_ on applicatio0_.space=space3_.id "
			+ " where space3_.space_token= :spaceToken and (extract(epoch from applicatio0_.last_vuln_change) > :timestamp OR extract(epoch from applicatio0_.last_scan) > :timestamp) and applicatio0_.id in (select application_id from app_constructs) "
			+ " order by applicatio0_.mvn_group, applicatio0_.artifact, applicatio0_.version )"
			+ " UNION "
			+ " (select distinct applicatio0_.id, applicatio0_.artifact, applicatio0_.created_at, applicatio0_.modified_at, applicatio0_.last_vuln_change, applicatio0_.last_scan, "
			+ " applicatio0_.mvn_group, applicatio0_.space, applicatio0_.version "
			+ " from app applicatio0_ inner join app_dependency dependenci1_ on applicatio0_.id=dependenci1_.app "
			+ " inner join space space2_ on applicatio0_.space=space2_.id where space2_.space_token= :spaceToken and (extract(epoch from applicatio0_.last_vuln_change) > :timestamp OR extract(epoch from applicatio0_.last_scan) > :timestamp)"
			+ " order by applicatio0_.mvn_group, applicatio0_.artifact, applicatio0_.version )", nativeQuery=true)
	List<Application> findNonEmptyApps(@Param("spaceToken") String spaceToken, @Param("timestamp") long timestamp );
	
	@Query("SELECT DISTINCT app FROM Application AS app JOIN FETCH app.dependencies d where d.lib.digest=:digest")
	List<Application> findAppsWithDigest(@Param("digest") String digest);
	
	/*@Query("SELECT"
	+ " DISTINCT b FROM"
	+ "   Bug b,"
	+ "   IN(b.constructChanges) cc,"
	+ "   Library l,"
	+ "   IN(l.constructs) lc,"
	+ "   Application a,"
	+ "   IN(a.dependencies) ad,"
	+ "   Dependency d"
	+ " WHERE"
	+ "       cc.constructId.lang  = lc.lang"
	+ "   AND cc.constructId.type  = lc.type"
	+ "   AND cc.constructId.qname = lc.qname"
	+ "   AND l.digest = d.lib"
	+ "   AND a.id = d.app"
	+ "   AND a.mvnGroup = :mvnGroup"
	+ "   AND a.artifact = :artifact"
	+ "   AND a.version = :version")*/
	@Query("SELECT"
			+ " DISTINCT b FROM"
			+ "   Bug b"
			+ "   JOIN b.constructChanges cc,"
			+ "   Library l"
			+ "   JOIN l.constructs lc,"
			+ "   Dependency d"
			+ "   JOIN d.app a"
			+ " WHERE"
			+ "       cc.constructId = lc"
			+ "   AND l.digest = d.lib"
		//	+ "   AND a.id = d.app"
			+ "   AND a.mvnGroup = :mvnGroup"
			+ "   AND a.artifact = :artifact"
			+ "   AND a.version = :version"
			+ "   AND a.space = :space"
			+ "   AND NOT lc.type='PACK'"
			)
	List<Bug> findBugsByGAV(@Param("mvnGroup") String group, @Param("artifact") String artifact, @Param("version") String version, @Param("space") Space space);
	
	@Query("SELECT"
			+ "   DISTINCT new com.sap.psr.vulas.backend.model.VulnerableDependency(d,b) FROM"
			+ "	  Dependency d "
			+ "   JOIN "
			+ "   d.app a "
			+ "	  JOIN "
			+ "   d.lib l"
			+ "   JOIN "
			+ "   l.libraryId dep_libid,"
			+ "	  Bug b"
			+ "   JOIN "
			+ "   b.affectedVersions av "
			+ "   JOIN "
			+ "   av.libraryId av_libid "
			+ "   LEFT OUTER JOIN "
			+ "   b.constructChanges as cc "
			+ "	  WHERE a.mvnGroup = :mvnGroup "
			+ "   AND a.artifact = :artifact "
			+ "   AND a.version = :version "
			+ "   AND a.space = :space "
			+ "   AND dep_libid = av_libid "
			+ "   AND av.affected = :affected " 
			+ "   AND cc IS NULL "
			)
	TreeSet<VulnerableDependency> findJPQLVulnerableDependenciesByGAVAndAffVersion(@Param("mvnGroup") String group, @Param("artifact") String artifact,@Param("version") String version, @Param("space") Space space, @Param("affected") Boolean affected);

	@Query("SELECT"
			+ "   DISTINCT new com.sap.psr.vulas.backend.model.VulnerableDependency(d,b) FROM"
			+ "	  Dependency d "
			+ "   JOIN "
			+ "   d.app a "
			+ "	  JOIN "
			+ "   d.lib l"
			+ "   JOIN "
			+ "   l.constructs lc,"
			+ "	  Bug b"
			+ "   JOIN "
			+ "   b.constructChanges cc "
			+ "	  WHERE a.mvnGroup = :mvnGroup "
			+ "   AND a.artifact = :artifact "
			+ "   AND a.version = :version"
			+ "   AND a.space = :space"
			+ "   AND lc = cc.constructId"		
			+ "   AND (NOT lc.type='PACK' "                        // Java + Python exception
			+ "   OR NOT EXISTS (SELECT 1 FROM ConstructChange cc1 JOIN cc1.constructId c1 WHERE cc1.bug=cc.bug AND NOT c1.type='PACK' AND NOT c1.qname LIKE '%test%' AND NOT c1.qname LIKE '%Test%' and NOT cc1.constructChangeType='ADD') ) "      //select bug if all other cc of the same bug are PACK, ADD or Test changes
			+ "   AND NOT (lc.type='MODU' AND (lc.qname='setup' OR lc.qname='tests' OR lc.qname='test.__init__')) " // Python-specific exception: setup.py is virtually everywhere, considering it would bring far too many FPs. Similarly tests.py originates such a generic module that would bring up too many FPs
			)
	TreeSet<VulnerableDependency> findJPQLVulnerableDependenciesByGAV(@Param("mvnGroup") String group, @Param("artifact") String artifact, @Param("version") String version, @Param("space") Space space);

	/**
	 * Counts the number of constructs of the given {@link ConstructType}s contained in the application's {@link Dependency}s (excluding those having the provided scopes).
	 * @param group
	 * @param artifact
	 * @param version
	 * @param type
	 * @return
	 */
	@Query("SELECT"
			+ "   COUNT(lc) FROM"
			+ "	  Dependency d "
			+ "   JOIN "
			+ "   d.app a "
			+ "	  JOIN "
			+ "   d.lib l"
			+ "   JOIN "
			+ "   l.constructs lc "
			+ "	  WHERE a.mvnGroup = :mvnGroup "
			+ "   AND a.artifact = :artifact "
			+ "   AND a.version = :version "
			+ "   AND a.space = :space "
			+ "   AND lc.type IN :types "
			+ "   AND d.scope NOT IN :exclScopes" 
			)
	Integer countDepConstructTypes(@Param("mvnGroup") String group, @Param("artifact") String artifact, @Param("version") String version, @Param("space") Space space, @Param("types") ConstructType[] types, @Param("exclScopes") Scope[] exclScopes);
	
	/**
	 * Counts the number of constructs of the given {@link ConstructType}s contained in the application's {@link Dependency}s.
	 * @param group
	 * @param artifact
	 * @param version
	 * @param type
	 * @return
	 */
	@Query("SELECT"
			+ "   COUNT(lc) FROM"
			+ "	  Dependency d "
			+ "   JOIN "
			+ "   d.app a "
			+ "	  JOIN "
			+ "   d.lib l"
			+ "   JOIN "
			+ "   l.constructs lc "
			+ "	  WHERE a.mvnGroup = :mvnGroup "
			+ "   AND a.artifact = :artifact "
			+ "   AND a.version = :version "
			+ "   AND a.space = :space "
			+ "   AND lc.type IN :types "
			)
	Integer countDepConstructTypes(@Param("mvnGroup") String group, @Param("artifact") String artifact, @Param("version") String version, @Param("space") Space space, @Param("types") ConstructType[] types);

	/**
	 * Counts the number of constructs of the given {@link ConstructType}s contained in the application's {@link Dependency}s.
	 * @param group
	 * @param artifact
	 * @param version
	 * @param type
	 * @return
	 */
	@Query("SELECT"
			+ "   DISTINCT new com.sap.psr.vulas.backend.model.ConstructSearchResult(d, lc) FROM"
			+ "	    Dependency d "
			+ "     JOIN "
			+ "     d.app a "
			+ "	    JOIN "
			+ "     d.lib l"
			+ "     JOIN "
			+ "     l.constructs lc "
			+ "	  WHERE a.mvnGroup = :mvnGroup "
			+ "     AND a.artifact = :artifact "
			+ "     AND a.version = :version "
			+ "     AND a.space = :space "
			+ "     AND lc.type IN :types "
			+ "     AND lc.qname LIKE :searchString "
			)
	TreeSet<ConstructSearchResult> searchDepConstructs(@Param("mvnGroup") String group, @Param("artifact") String artifact, @Param("version") String version, @Param("space") Space space, @Param("types") ConstructType[] types, @Param("searchString") String searchString);
	
	/**
	 * Counts the number of {@link Dependency}s of the given {@link Application}.
	 * @param group
	 * @param artifact
	 * @param version
	 * @param type
	 * @return
	 */
	@Query("SELECT"
			+ "   COUNT(d) FROM"
			+ "	    Dependency d "
			+ "     JOIN "
			+ "     d.app a "
			+ "	    JOIN "
			+ "     d.lib l"
			+ "	  WHERE a.mvnGroup = :mvnGroup "
			+ "     AND a.artifact = :artifact "
			+ "     AND a.version = :version "
			+ "     AND a.space = :space" 
			)
	Integer countDependencies(@Param("mvnGroup") String group, @Param("artifact") String artifact, @Param("version") String version, @Param("space") Space space);
	
	/**
	 * Counts the number of {@link Dependency}s of the given {@link Application} (excluding those having the provided scopes).
	 * @param group
	 * @param artifact
	 * @param version
	 * @param type
	 * @return
	 */
	@Query("SELECT"
			+ "   COUNT(d) FROM"
			+ "	    Dependency d "
			+ "     JOIN "
			+ "     d.app a "
			+ "	    JOIN "
			+ "     d.lib l"
			+ "	  WHERE a.mvnGroup = :mvnGroup "
			+ "     AND a.artifact = :artifact "
			+ "     AND a.version = :version "
			+ "     AND a.space = :space "
			+ "     AND d.scope NOT IN :exclScopes" 
			)
	Integer countDependencies(@Param("mvnGroup") String group, @Param("artifact") String artifact, @Param("version") String version, @Param("space") Space space, @Param("exclScopes") Scope[] exclScopes);
	
	@Query("SELECT"
			+ "   DISTINCT new com.sap.psr.vulas.backend.model.VulnerableDependency(d,b) FROM"
			+ "	  Dependency d "
			+ "   JOIN "
			+ "   d.app a "
			+ "	  JOIN "
			+ "   d.lib l"
			+ "   JOIN "
			+ "   l.constructs lc,"
			+ "	  Bug b"
			+ "   JOIN "
			+ "   b.constructChanges cc "
			+ "	  WHERE lc = cc.constructId"
			+ "   AND NOT lc.type='PACK'" 
			)
	TreeSet<VulnerableDependency> findJPQLVulnerableDependencies();

	/*@Query("SELECT"
		+ "   DISTINCT new com.sap.psr.vulas.backend.model.VulnerableDependency(vd.dep,vd.bug) FROM"
		+ "	  VulnerableDependencyView vd "
		+ "	  WHERE vd.mvnGroup = :mvnGroup "
		+ "   AND vd.artifact = :artifact "
		+ "   AND vd.version = :version"
		)
	List<VulnerableDependency> findJPQLVulnerableDependenciesByGAVView(@Param("mvnGroup") String group, @Param("artifact") String artifact,@Param("version") String version);*/
	
	/**
	 * Finds the applications whose dependencies include constructs from the given list.
	 * Note: the outer select a was added because the lock does not allow the use of "distinct" and we want to avoid to update the same application multiple times in the subsequent update query
	 * @param listOfConstructs list of {@link ConstructId}
	 * @return list of {@link Application}
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Transactional
	@Query("select a from Application a where a in"
			+ "( SELECT distinct d.app FROM Dependency d "
			+ "	  JOIN "
			+ "   d.lib l"
			+ "   JOIN "
			+ "   l.constructs lc "
			+ "	  WHERE lc IN :listOfConstructs "		
			+ "   AND (NOT lc.type='PACK' "                        // Java + Python exception
			+ "   OR NOT EXISTS (SELECT 1 FROM ConstructChange cc1 JOIN cc1.constructId c1 WHERE c1 IN :listOfConstructs AND NOT c1.type='PACK' AND NOT c1.qname LIKE '%test%' AND NOT c1.qname LIKE '%Test%' and NOT cc1.constructChangeType='ADD') ) "     
			+ "   AND NOT (lc.type='MODU' AND (lc.qname='setup' OR lc.qname='tests' OR lc.qname='test.__init__')) )" // Python-specific exception: setup.py is virtually everywhere, considering it would bring far too many FPs. Similarly tests.py originates such a generic module that would bring up too many FPs
			)
	List<Application> findAppsByCC(@Param("listOfConstructs") List<ConstructId> listOfConstructs);
	
	/**
	 * Finds the applications whose dependencies include {@link LibraryId}s from the given list.
	 * Note: the outer select a was added because the lock does not allow the use of "distinct" and we want to avoid to update the same application multiple times in the subsequent update query
	 * @param listOfConstructs list of {@link ConstructId}
	 * @return list of {@link Application}
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Transactional
	@Query("select a from Application a where a in"
			+ "( SELECT distinct d.app FROM Dependency d "
			+ "	  JOIN "
			+ "   d.lib l"
			+ "   JOIN "
			+ "   l.libraryId dep_libid"
			+ "	  WHERE dep_libid = :affLibId )"	
			)
	List<Application> findAppsByAffLib(@Param("affLibId") LibraryId affLibId);
	
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query(value="UPDATE Application app SET last_vuln_change=CURRENT_TIMESTAMP where app in :listOfApp " 
			)
	void updateAppLastVulnChange(@Param("listOfApp") List<Application> listOfApp);
	
	
}
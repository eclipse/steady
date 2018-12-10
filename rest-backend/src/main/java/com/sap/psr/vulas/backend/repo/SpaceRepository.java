package com.sap.psr.vulas.backend.repo;


import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sap.psr.vulas.backend.model.Space;
import com.sap.psr.vulas.backend.model.Tenant;
import com.sap.psr.vulas.backend.util.ResultSetFilter;

@Repository
public interface SpaceRepository extends CrudRepository<Space, Long>, SpaceRepositoryCustom {

	public static final ResultSetFilter<Space> FILTER = new ResultSetFilter<Space>();
	
	List<Space> findById(@Param("id") Long id);
	
	/**
	 * All spaces of the given {@link Tenant}.
	 * @param tenant as String
	 * @param token
	 * @return
	 */
	@Query("SELECT s FROM Space AS s LEFT JOIN FETCH s.spaceOwners WHERE s.tenant.tenantToken = :tenant")
	List<Space> findAllTenantSpaces(@Param("tenant") String tenant);
	
	/**
	 * All spaces of the given {@link Tenant} with public visibility.
	 * @param tenant as String
	 * @param token
	 * @return
	 */
	@Query("SELECT s FROM Space AS s LEFT JOIN FETCH s.spaceOwners WHERE s.tenant.tenantToken = :tenant and s.isPublic = :p")
	List<Space> findAllTenantSpaces(@Param("tenant") String tenant, @Param("p") boolean p);
	
	/**
	 * Should return just one space.
	 * @param token
	 * @return
	 */
	@Query("SELECT s FROM Space AS s LEFT JOIN FETCH s.spaceOwners WHERE s.spaceToken = :token")
	List<Space> findBySecondaryKey(@Param("token") String token);
	
	/**
	 * Should return just one space.
	 * @param tenant as String
	 * @param token
	 * @return
	 */
	@Query("SELECT s FROM Space AS s LEFT JOIN FETCH s.spaceOwners WHERE s.tenant = :tenant and s.spaceToken = :token")
	List<Space> findBySecondaryKey(@Param("tenant") Tenant tenant, @Param("token") String token);

	/**
	 * Should return just one space.
	 * @param tenant as String
	 * @param token
	 * @return
	 */
	@Query("SELECT s FROM Space AS s LEFT JOIN FETCH  s.tenant t WHERE t.tenantToken = :tenant and s.spaceToken = :token")
	List<Space> findBySecondaryKey(@Param("tenant") String tenant, @Param("token") String token);
	
	/**
	 * Should return just one space: the default space for the given tenant.
	 * @param tenant as String
	 * @return
	 */
	@Query("SELECT s FROM Space AS s LEFT JOIN FETCH s.spaceOwners WHERE s.tenant.tenantToken = :tenant and s.isDefault = true")
	Space  findDefault(@Param("tenant") String tenant);
}
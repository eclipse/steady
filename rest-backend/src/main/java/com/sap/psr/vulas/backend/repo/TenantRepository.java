package com.sap.psr.vulas.backend.repo;


import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sap.psr.vulas.backend.model.Tenant;
import com.sap.psr.vulas.backend.util.ResultSetFilter;

@Repository
public interface TenantRepository extends CrudRepository<Tenant, Long>, TenantRepositoryCustom {

	public static final ResultSetFilter<Tenant> FILTER = new ResultSetFilter<Tenant>();
	
//	List<Tenant> findById(@Param("id") Long id);
	
	@Query("SELECT s FROM Tenant AS s WHERE s.tenantToken = :token")
	List<Tenant> findBySecondaryKey(@Param("token") String token);
	
	@Query("SELECT s FROM Tenant AS s WHERE s.isDefault=true")
	Tenant findDefault();
}
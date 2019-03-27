package com.sap.psr.vulas.backend.repo;


import com.sap.psr.vulas.backend.model.Tenant;

/**
 * Specifies additional methods of the {@link TenantRepository}.
 *
 */
public interface TenantRepositoryCustom {
	
	/**
	 * Checks that the tenant contains all user-provided info.
	 */
	public Boolean isTenantComplete(Tenant _tenant);
	
	/**
	 * Returns the tenant for the given tenant token (default tenant if token is null).
	 * @return
	 */
	public Tenant getTenant(String _tenantToken);
}

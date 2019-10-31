package com.sap.psr.vulas.backend.repo;

import com.sap.psr.vulas.backend.model.Tenant;

/** Specifies additional methods of the {@link TenantRepository}. */
public interface TenantRepositoryCustom {

  /**
   * Checks that the tenant contains all user-provided info.
   *
   * @param _tenant a {@link com.sap.psr.vulas.backend.model.Tenant} object.
   * @return a {@link java.lang.Boolean} object.
   */
  public Boolean isTenantComplete(Tenant _tenant);

  /**
   * Returns the tenant for the given tenant token (default tenant if token is null).
   *
   * @param _tenantToken a {@link java.lang.String} object.
   * @return a {@link com.sap.psr.vulas.backend.model.Tenant} object.
   */
  public Tenant getTenant(String _tenantToken);
}

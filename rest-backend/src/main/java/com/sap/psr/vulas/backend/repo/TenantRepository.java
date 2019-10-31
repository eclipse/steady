package com.sap.psr.vulas.backend.repo;

import com.sap.psr.vulas.backend.model.Tenant;
import com.sap.psr.vulas.backend.util.ResultSetFilter;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** TenantRepository interface. */
@Repository
public interface TenantRepository extends CrudRepository<Tenant, Long>, TenantRepositoryCustom {

  /** Constant <code>FILTER</code> */
  public static final ResultSetFilter<Tenant> FILTER = new ResultSetFilter<Tenant>();

  /**
   * findById.
   *
   * @param id a {@link java.lang.Long} object.
   * @return a {@link java.util.List} object.
   */
  List<Tenant> findById(@Param("id") Long id);

  /**
   * findBySecondaryKey.
   *
   * @param token a {@link java.lang.String} object.
   * @return a {@link java.util.List} object.
   */
  @Query("SELECT s FROM Tenant AS s WHERE s.tenantToken = :token")
  List<Tenant> findBySecondaryKey(@Param("token") String token);

  /**
   * findDefault.
   *
   * @return a {@link com.sap.psr.vulas.backend.model.Tenant} object.
   */
  @Query("SELECT s FROM Tenant AS s WHERE s.isDefault=true")
  Tenant findDefault();
}

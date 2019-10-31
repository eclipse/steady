package com.sap.psr.vulas.backend.repo;

import com.sap.psr.vulas.backend.model.Property;
import com.sap.psr.vulas.backend.util.ResultSetFilter;
import com.sap.psr.vulas.shared.enums.PropertySource;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** PropertyRepository interface. */
@Repository
public interface PropertyRepository extends CrudRepository<Property, Long> {

  /** Constant <code>FILTER</code> */
  public static final ResultSetFilter<Property> FILTER = new ResultSetFilter<Property>();

  /**
   * findBySecondaryKey.
   *
   * @param source a {@link com.sap.psr.vulas.shared.enums.PropertySource} object.
   * @param name a {@link java.lang.String} object.
   * @param value a {@link java.lang.String} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT prop FROM Property AS prop WHERE prop.source = :source AND prop.name = :name AND prop.propertyValue = :value")
  List<Property> findBySecondaryKey(
      @Param("source") PropertySource source,
      @Param("name") String name,
      @Param("value") String value);
}

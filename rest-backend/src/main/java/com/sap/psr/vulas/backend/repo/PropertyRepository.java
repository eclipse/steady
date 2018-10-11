package com.sap.psr.vulas.backend.repo;


import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sap.psr.vulas.backend.model.Property;
import com.sap.psr.vulas.backend.util.ResultSetFilter;
import com.sap.psr.vulas.shared.enums.PropertySource;

@Repository
public interface PropertyRepository extends CrudRepository<Property, Long> {

	public static final ResultSetFilter<Property> FILTER = new ResultSetFilter<Property>();
	
	@Query("SELECT prop FROM Property AS prop WHERE prop.source = :source AND prop.name = :name AND prop.propertyValue = :value")
	List<Property> findBySecondaryKey(@Param("source") PropertySource source, @Param("name") String name, @Param("value") String value);
}